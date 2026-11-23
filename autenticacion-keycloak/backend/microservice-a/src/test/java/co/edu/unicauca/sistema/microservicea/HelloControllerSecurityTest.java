package co.edu.unicauca.sistema.microservicea;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Exercises the real security filter chain. Only the {@link JwtDecoder} is replaced (so no Keycloak is
 * needed): the bearer-token filter, the role converter, URL rules and {@code @PreAuthorize} all run.
 */
@SpringBootTest
@AutoConfigureMockMvc
class HelloControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    /** Registers a token value that the mocked decoder resolves to a user with the given client roles. */
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
        when(jwtDecoder.decode(tokenValue)).thenReturn(jwt);
    }

    /** Authorization matrix: user, role of the user, endpoint and expected HTTP status. */
    @ParameterizedTest(name = "{0} ({1}) -> {2} => {3}")
    @CsvSource({
            "juan,    student,     /hello_student,     200",
            "juan,    student,     /hello_coordinator, 403",
            "juan,    student,     /hello_company,     403",
            "juan,    student,     /hello_all,         200",
            "julio,   coordinator, /hello_coordinator, 200",
            "julio,   coordinator, /hello_student,     403",
            "julio,   coordinator, /hello_all,         200",
            "company1, company,    /hello_company,     200",
            "company1, company,    /hello_student,     403",
            "company1, company,    /hello_all,         200"
    })
    @DisplayName("Role-based authorization matrix")
    void authorizationMatrix(String user, String role, String endpoint, int expectedStatus) throws Exception {
        givenToken("token-" + user, user, List.of(role));

        mockMvc.perform(get(endpoint).header("Authorization", "Bearer token-" + user))
                .andExpect(status().is(expectedStatus));
    }

    @Test
    @DisplayName("Allowed call returns the service greeting")
    void allowedCallReturnsBody() throws Exception {
        givenToken("token-juan", "juan", List.of("student"));

        mockMvc.perform(get("/hello_student").header("Authorization", "Bearer token-juan"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello student from microservice-a"));
    }

    @ParameterizedTest(name = "no token -> {0} => 401")
    @CsvSource({"/hello_student", "/hello_coordinator", "/hello_company", "/hello_all"})
    @DisplayName("Requests without a token get 401 Unauthorized")
    void withoutTokenIsUnauthorized(String endpoint) throws Exception {
        mockMvc.perform(get(endpoint))
                .andExpect(status().isUnauthorized())
                .andExpect(header().exists("WWW-Authenticate"));
    }

    @Test
    @DisplayName("Invalid or expired token gets 401 Unauthorized")
    void invalidTokenIsUnauthorized() throws Exception {
        when(jwtDecoder.decode("expired")).thenThrow(new BadJwtException("Jwt expired"));

        mockMvc.perform(get("/hello_all").header("Authorization", "Bearer expired"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Valid token without roles can only use hello_all")
    void tokenWithoutRolesOnlyReachesHelloAll() throws Exception {
        givenToken("token-nobody", "nobody", List.of());

        mockMvc.perform(get("/hello_all").header("Authorization", "Bearer token-nobody"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/hello_student").header("Authorization", "Bearer token-nobody"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("A user with several roles reaches every matching endpoint")
    void multipleRolesAreAllHonoured() throws Exception {
        givenToken("token-multi", "multi", List.of("student", "company"));

        mockMvc.perform(get("/hello_student").header("Authorization", "Bearer token-multi"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/hello_company").header("Authorization", "Bearer token-multi"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/hello_coordinator").header("Authorization", "Bearer token-multi"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Unknown paths are denied")
    void unknownPathsAreDenied() throws Exception {
        givenToken("token-juan", "juan", List.of("student"));

        mockMvc.perform(get("/admin").header("Authorization", "Bearer token-juan"))
                .andExpect(status().isForbidden());
    }
}
