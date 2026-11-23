package co.edu.unicauca.sistema.microservicea;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

/** Unit tests of the Keycloak role extraction (no Spring context needed). */
class KeycloakRoleConverterTest {

    private final KeycloakRoleConverter converter = new KeycloakRoleConverter();

    private static Jwt jwtWithClaims(Map<String, Object> claims) {
        Jwt.Builder builder = Jwt.withTokenValue("token")
                .header("alg", "none")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .claim("sub", "test-user");
        claims.forEach(builder::claim);
        return builder.build();
    }

    private static List<String> names(Jwt jwt) {
        return new KeycloakRoleConverter().convert(jwt).stream().map(GrantedAuthority::getAuthority).toList();
    }

    @Test
    @DisplayName("Client roles become ROLE_ authorities")
    void mapsClientRolesToRolePrefixedAuthorities() {
        Jwt jwt = jwtWithClaims(Map.of("resource_access",
                Map.of("sistema-desktop", Map.of("roles", List.of("student", "company")))));

        assertThat(names(jwt)).containsExactlyInAnyOrder("ROLE_student", "ROLE_company");
    }

    @Test
    @DisplayName("Token without resource_access yields no authorities")
    void returnsEmptyWhenClaimIsMissing() {
        assertThat(converter.convert(jwtWithClaims(Map.of()))).isEmpty();
    }

    @Test
    @DisplayName("Roles of other clients are ignored")
    void ignoresRolesOfOtherClients() {
        Jwt jwt = jwtWithClaims(Map.of("resource_access",
                Map.of("another-client", Map.of("roles", List.of("coordinator")))));

        assertThat(converter.convert(jwt)).isEmpty();
    }

    @Test
    @DisplayName("Realm roles (realm_access) are not trusted for authorization")
    void ignoresRealmRoles() {
        Jwt jwt = jwtWithClaims(Map.of("realm_access", Map.of("roles", List.of("coordinator"))));

        assertThat(converter.convert(jwt)).isEmpty();
    }

    @Test
    @DisplayName("Malformed claims do not throw")
    void toleratesMalformedClaims() {
        assertThat(converter.convert(jwtWithClaims(Map.of("resource_access", "oops")))).isEmpty();
        assertThat(converter.convert(jwtWithClaims(Map.of("resource_access",
                Map.of("sistema-desktop", "oops"))))).isEmpty();
        assertThat(converter.convert(jwtWithClaims(Map.of("resource_access",
                Map.of("sistema-desktop", Map.of("roles", "student")))))).isEmpty();
    }

    @Test
    @DisplayName("Non-string role entries are skipped")
    void skipsNonStringRoles() {
        Jwt jwt = jwtWithClaims(Map.of("resource_access",
                Map.of("sistema-desktop", Map.of("roles", List.of("student", 42)))));

        assertThat(names(jwt)).containsExactly("ROLE_student");
    }
}
