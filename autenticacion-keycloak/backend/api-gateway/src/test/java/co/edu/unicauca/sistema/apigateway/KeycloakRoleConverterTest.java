package co.edu.unicauca.sistema.apigateway;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

/** Unit tests of the role extraction used by the gateway. */
class KeycloakRoleConverterTest {

    private final KeycloakRoleConverter converter = new KeycloakRoleConverter();

    private static Jwt jwt(Map<String, Object> claims) {
        Jwt.Builder builder = Jwt.withTokenValue("t").header("alg", "none")
                .issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(60)).claim("sub", "u");
        claims.forEach(builder::claim);
        return builder.build();
    }

    @Test
    @DisplayName("Client roles become ROLE_ authorities")
    void mapsRoles() {
        Jwt token = jwt(Map.of("resource_access",
                Map.of("sistema-desktop", Map.of("roles", List.of("coordinator")))));

        assertThat(converter.convert(token).stream().map(GrantedAuthority::getAuthority))
                .containsExactly("ROLE_coordinator");
    }

    @Test
    @DisplayName("Missing or malformed claims yield no authorities")
    void toleratesMissingClaims() {
        assertThat(converter.convert(jwt(Map.of()))).isEmpty();
        assertThat(converter.convert(jwt(Map.of("resource_access", Map.of("other", Map.of("roles", List.of("student"))))))).isEmpty();
        assertThat(converter.convert(jwt(Map.of("resource_access", "oops")))).isEmpty();
    }
}
