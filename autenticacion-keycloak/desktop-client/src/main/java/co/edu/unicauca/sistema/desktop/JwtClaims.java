package co.edu.unicauca.sistema.desktop;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Reads the claims of a JWT <strong>only to display them</strong> (user name and roles in the UI).
 *
 * <p>The client does not verify the signature and must never take security decisions from these
 * values: the real validation and authorization are performed by the API Gateway and the
 * microservices.</p>
 */
public final class JwtClaims {

    private final JSONObject payload;

    private JwtClaims(JSONObject payload) {
        this.payload = payload;
    }

    /**
     * Decodes the payload (second Base64URL segment) of a compact JWT.
     *
     * @throws IllegalArgumentException if the token is not a well-formed JWT
     */
    public static JwtClaims parse(String token) {
        if (token == null) {
            throw new IllegalArgumentException("Token is null");
        }
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("A JWT must have three segments");
        }
        try {
            String json = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            return new JwtClaims(new JSONObject(json));
        } catch (IllegalArgumentException | JSONException e) {
            throw new IllegalArgumentException("The JWT payload cannot be decoded", e);
        }
    }

    /** User name ({@code preferred_username}), or an empty string if the claim is absent. */
    public String username() {
        return payload.optString("preferred_username", "");
    }

    /** Roles of the given Keycloak client, taken from {@code resource_access.<clientId>.roles}. */
    public List<String> rolesOf(String clientId) {
        JSONObject resourceAccess = payload.optJSONObject("resource_access");
        if (resourceAccess == null) {
            return Collections.emptyList();
        }
        JSONObject client = resourceAccess.optJSONObject(clientId);
        if (client == null) {
            return Collections.emptyList();
        }
        JSONArray roles = client.optJSONArray("roles");
        if (roles == null) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<>();
        for (int i = 0; i < roles.length(); i++) {
            result.add(roles.optString(i));
        }
        return List.copyOf(result);
    }

    /** Expiration instant in epoch seconds, or {@code 0} if absent. */
    public long expiresAtEpochSeconds() {
        return payload.optLong("exp", 0L);
    }
}
