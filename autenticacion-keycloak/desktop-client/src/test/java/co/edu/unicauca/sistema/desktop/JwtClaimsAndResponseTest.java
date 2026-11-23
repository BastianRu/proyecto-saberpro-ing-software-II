package co.edu.unicauca.sistema.desktop;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Unit tests of the JWT payload reader and of the response-to-message mapping. */
class JwtClaimsAndResponseTest {

    private static String tokenWithPayload(String payloadJson) {
        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        return encoder.encodeToString("{\"alg\":\"RS256\"}".getBytes(StandardCharsets.UTF_8))
                + "." + encoder.encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8))
                + ".signature";
    }

    @Test
    @DisplayName("Reads user name and client roles from the token payload")
    void readsUsernameAndRoles() {
        JwtClaims claims = JwtClaims.parse(tokenWithPayload("""
                {"preferred_username":"julio","exp":1900000000,
                 "resource_access":{"sistema-desktop":{"roles":["coordinator"]}}}"""));

        assertThat(claims.username()).isEqualTo("julio");
        assertThat(claims.rolesOf("sistema-desktop")).containsExactly("coordinator");
        assertThat(claims.expiresAtEpochSeconds()).isEqualTo(1900000000L);
    }

    @Test
    @DisplayName("Roles of another client or a missing claim give an empty list")
    void missingRolesAreEmpty() {
        JwtClaims claims = JwtClaims.parse(tokenWithPayload("{\"preferred_username\":\"x\"}"));

        assertThat(claims.rolesOf("sistema-desktop")).isEmpty();
        assertThat(JwtClaims.parse(tokenWithPayload(
                "{\"resource_access\":{\"other\":{\"roles\":[\"student\"]}}}")).rolesOf("sistema-desktop")).isEmpty();
    }

    @Test
    @DisplayName("Malformed tokens are rejected with IllegalArgumentException")
    void malformedTokensAreRejected() {
        assertThatThrownBy(() -> JwtClaims.parse(null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> JwtClaims.parse("only.two")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> JwtClaims.parse("a.%%%.c")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> JwtClaims.parse("a." + Base64.getUrlEncoder().encodeToString("no json".getBytes(StandardCharsets.UTF_8)) + ".c"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("401 and 403 get a user-facing explanation")
    void securityStatusesAreExplained() {
        assertThat(new ApiResponse(401, "").describe()).contains("401").contains("Log in again");
        assertThat(new ApiResponse(403, "").describe()).contains("403").contains("role");
    }

    @Test
    @DisplayName("2xx shows the body; other statuses are flagged")
    void successAndOtherStatuses() {
        assertThat(new ApiResponse(200, "Hello").describe()).isEqualTo("200 OK: Hello");
        assertThat(new ApiResponse(200, "x").isSuccess()).isTrue();
        assertThat(new ApiResponse(503, "").describe()).contains("could not reach");
        assertThat(new ApiResponse(500, "boom").describe()).contains("Unexpected response");
        assertThat(new ApiResponse(404, "").isSuccess()).isFalse();
    }
}
