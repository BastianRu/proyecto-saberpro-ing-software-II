package co.edu.unicauca.sistema.desktop;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Tests the token request against a fake Keycloak implemented with the JDK HTTP server. */
class KeycloakClientTest {

    private HttpServer fakeKeycloak;
    private final AtomicReference<String> receivedBody = new AtomicReference<>();
    private final AtomicReference<String> receivedContentType = new AtomicReference<>();
    private int replyStatus;
    private String replyBody;

    @BeforeEach
    void startFakeKeycloak() throws IOException {
        fakeKeycloak = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        fakeKeycloak.createContext("/realms/sistema/protocol/openid-connect/token", exchange -> {
            receivedBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            receivedContentType.set(exchange.getRequestHeaders().getFirst("Content-Type"));
            byte[] body = replyBody.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(replyStatus, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        fakeKeycloak.start();
    }

    @AfterEach
    void stopFakeKeycloak() {
        fakeKeycloak.stop(0);
    }

    private KeycloakClient client() {
        String base = "http://127.0.0.1:" + fakeKeycloak.getAddress().getPort();
        return new KeycloakClient(new ClientConfig(
                base + "/realms/sistema/protocol/openid-connect/token", base, "sistema-desktop"));
    }

    @Test
    @DisplayName("Successful login returns the access token and sends a password-grant form")
    void loginReturnsAccessToken() throws Exception {
        replyStatus = 200;
        replyBody = "{\"access_token\":\"abc.def.ghi\",\"expires_in\":300,\"token_type\":\"Bearer\"}";

        String token = client().login("juan", "123");

        assertThat(token).isEqualTo("abc.def.ghi");
        assertThat(receivedContentType.get()).isEqualTo("application/x-www-form-urlencoded");
        assertThat(receivedBody.get()).isEqualTo(
                "grant_type=password&client_id=sistema-desktop&username=juan&password=123");
    }

    @Test
    @DisplayName("Invalid credentials raise AuthenticationException with Keycloak's description")
    void invalidCredentialsAreReported() {
        replyStatus = 401;
        replyBody = "{\"error\":\"invalid_grant\",\"error_description\":\"Invalid user credentials\"}";

        assertThatThrownBy(() -> client().login("juan", "wrong"))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("401")
                .hasMessageContaining("Invalid user credentials");
    }

    @Test
    @DisplayName("A non-JSON reply is reported as an unexpected response")
    void nonJsonReplyIsReported() {
        replyStatus = 502;
        replyBody = "<html>Bad gateway</html>";

        assertThatThrownBy(() -> client().login("juan", "123"))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("Unexpected reply");
    }

    @Test
    @DisplayName("Keycloak down: AuthenticationException, no raw network exception")
    void unreachableKeycloakIsReported() {
        KeycloakClient unreachable = new KeycloakClient(new ClientConfig(
                "http://127.0.0.1:1/realms/sistema/protocol/openid-connect/token",
                "http://127.0.0.1:1", "sistema-desktop"));

        assertThatThrownBy(() -> unreachable.login("juan", "123"))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("not reachable");
    }

    @Test
    @DisplayName("Blank username or empty password is rejected without calling Keycloak")
    void blankCredentialsAreRejectedLocally() {
        assertThatThrownBy(() -> client().login(" ", "123")).isInstanceOf(AuthenticationException.class);
        assertThatThrownBy(() -> client().login("juan", "")).isInstanceOf(AuthenticationException.class);
        assertThat(receivedBody.get()).isNull();
    }

    @Test
    @DisplayName("Special characters in the password are percent-encoded")
    void formBodyEncodesSpecialCharacters() {
        String body = KeycloakClient.buildTokenRequestBody("sistema-desktop", "ana maria", "p&ss=w ord%");

        assertThat(body).isEqualTo(
                "grant_type=password&client_id=sistema-desktop&username=ana+maria&password=p%26ss%3Dw+ord%25");
    }
}
