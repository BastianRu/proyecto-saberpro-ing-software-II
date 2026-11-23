package co.edu.unicauca.sistema.apigateway;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

/**
 * Tests the gateway end to end with the real security chain and the real routes. The only fakes are
 * the {@link ReactiveJwtDecoder} (instead of Keycloak) and two tiny HTTP servers that play the role of
 * microservice-a and microservice-b, which lets us verify route rewriting and JWT propagation.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class GatewayRoutingSecurityTest {

    /** Minimal stand-in for a downstream microservice; records what the gateway forwards to it. */
    private static final class StubService {
        final HttpServer server;
        final AtomicReference<String> lastPath = new AtomicReference<>();
        final AtomicReference<String> lastAuthorization = new AtomicReference<>();

        StubService(String name) {
            try {
                server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
            server.createContext("/", exchange -> {
                lastPath.set(exchange.getRequestURI().getPath());
                lastAuthorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
                byte[] body = ("stub " + name + " " + exchange.getRequestURI().getPath())
                        .getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(200, body.length);
                exchange.getResponseBody().write(body);
                exchange.close();
            });
            server.start();
        }

        String url() {
            return "http://127.0.0.1:" + server.getAddress().getPort();
        }

        void reset() {
            lastPath.set(null);
            lastAuthorization.set(null);
        }
    }

    private static final StubService SERVICE_A = new StubService("a");
    private static final StubService SERVICE_B = new StubService("b");

    @DynamicPropertySource
    static void downstreamUrls(DynamicPropertyRegistry registry) {
        registry.add("services.microservice-a.url", SERVICE_A::url);
        registry.add("services.microservice-b.url", SERVICE_B::url);
    }

    @AfterAll
    static void stopStubs() {
        SERVICE_A.server.stop(0);
        SERVICE_B.server.stop(0);
    }

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private ReactiveJwtDecoder jwtDecoder;

    @BeforeEach
    void resetStubs() {
        SERVICE_A.reset();
        SERVICE_B.reset();
    }

    private void givenToken(String tokenValue, String username, List<String> roles) {
        Instant now = Instant.now();
        Jwt jwt = Jwt.withTokenValue(tokenValue)
                .header("alg", "RS256")
                .subject(username)
                .claim("preferred_username", username)
                .claim("resource_access", Map.of("sistema-desktop", Map.of("roles", roles)))
                .issuedAt(now)
                .expiresAt(now.plusSeconds(300))
                .build();
        when(jwtDecoder.decode(tokenValue)).thenReturn(Mono.just(jwt));
    }

    @ParameterizedTest(name = "{0} ({1}) -> {2} => {3}")
    @CsvSource({
            "juan,     student,     /a/hello_student,     200",
            "juan,     student,     /a/hello_coordinator, 403",
            "juan,     student,     /b/hello_company,     403",
            "julio,    coordinator, /b/hello_coordinator, 200",
            "julio,    coordinator, /a/hello_student,     403",
            "company1, company,    /b/hello_company,      200",
            "company1, company,    /a/hello_company,      200",
            "company1, company,    /b/hello_student,      403",
            "juan,     student,     /a/hello_all,         200",
            "julio,    coordinator, /b/hello_all,         200",
            "company1, company,    /b/hello_all,          200"
    })
    @DisplayName("Gateway authorizes by role before routing")
    void gatewayAuthorizationMatrix(String user, String role, String path, int expectedStatus) {
        givenToken("token-" + user, user, List.of(role));

        webTestClient.get().uri(path)
                .header("Authorization", "Bearer token-" + user)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus);
    }

    @ParameterizedTest(name = "no token -> {0} => 401")
    @CsvSource({"/a/hello_student", "/a/hello_all", "/b/hello_coordinator", "/b/hello_all"})
    @DisplayName("No token: 401 and the request never reaches the microservice")
    void withoutTokenIsUnauthorized(String path) {
        webTestClient.get().uri(path)
                .exchange()
                .expectStatus().isUnauthorized();

        assertThat(SERVICE_A.lastPath.get()).isNull();
        assertThat(SERVICE_B.lastPath.get()).isNull();
    }

    @Test
    @DisplayName("Invalid or expired token gets 401")
    void invalidTokenIsUnauthorized() {
        when(jwtDecoder.decode("expired")).thenReturn(Mono.error(new BadJwtException("Jwt expired")));

        webTestClient.get().uri("/a/hello_all")
                .header("Authorization", "Bearer expired")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("Route /a/** goes to microservice-a stripping the prefix, and the JWT is propagated")
    void routesToServiceAAndPropagatesToken() {
        givenToken("token-juan", "juan", List.of("student"));

        webTestClient.get().uri("/a/hello_student")
                .header("Authorization", "Bearer token-juan")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("stub a /hello_student");

        assertThat(SERVICE_A.lastPath.get()).isEqualTo("/hello_student");
        assertThat(SERVICE_A.lastAuthorization.get()).isEqualTo("Bearer token-juan");
        assertThat(SERVICE_B.lastPath.get()).isNull();
    }

    @Test
    @DisplayName("Route /b/** goes to microservice-b stripping the prefix, and the JWT is propagated")
    void routesToServiceBAndPropagatesToken() {
        givenToken("token-julio", "julio", List.of("coordinator"));

        webTestClient.get().uri("/b/hello_coordinator")
                .header("Authorization", "Bearer token-julio")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("stub b /hello_coordinator");

        assertThat(SERVICE_B.lastPath.get()).isEqualTo("/hello_coordinator");
        assertThat(SERVICE_B.lastAuthorization.get()).isEqualTo("Bearer token-julio");
        assertThat(SERVICE_A.lastPath.get()).isNull();
    }

    @Test
    @DisplayName("Forbidden requests are stopped at the gateway (nothing reaches the microservice)")
    void forbiddenRequestsAreNotForwarded() {
        givenToken("token-juan", "juan", List.of("student"));

        webTestClient.get().uri("/a/hello_coordinator")
                .header("Authorization", "Bearer token-juan")
                .exchange()
                .expectStatus().isForbidden();

        assertThat(SERVICE_A.lastPath.get()).isNull();
    }

    @Test
    @DisplayName("Paths outside the declared endpoints are denied")
    void unknownPathsAreDenied() {
        givenToken("token-juan", "juan", List.of("student"));

        webTestClient.get().uri("/c/anything")
                .header("Authorization", "Bearer token-juan")
                .exchange()
                .expectStatus().isForbidden();
    }
}
