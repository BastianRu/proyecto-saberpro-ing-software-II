package co.edu.unicauca.sistema.desktop;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Obtains an access token from Keycloak using the OAuth2 <em>Resource Owner Password Credentials</em>
 * grant ("Direct access grants" in Keycloak): the user types credentials into the desktop application
 * and the application exchanges them for a JWT.
 */
public class KeycloakClient {

    private final ClientConfig config;
    private final HttpClient httpClient;

    public KeycloakClient(ClientConfig config) {
        this.config = config;
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    }

    /**
     * Builds the {@code application/x-www-form-urlencoded} body of the token request.
     * Every value is percent-encoded, so passwords containing {@code &}, {@code =} or spaces are safe.
     */
    static String buildTokenRequestBody(String clientId, String username, String password) {
        Map<String, String> form = new LinkedHashMap<>();
        form.put("grant_type", "password");
        form.put("client_id", clientId);
        form.put("username", username);
        form.put("password", password);
        return form.entrySet().stream()
                .map(e -> encode(e.getKey()) + "=" + encode(e.getValue()))
                .collect(Collectors.joining("&"));
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    /**
     * Requests a token for the given user.
     *
     * @return the raw access token (a signed JWT)
     * @throws AuthenticationException if Keycloak rejects the credentials or cannot be reached
     */
    public String login(String username, String password) throws AuthenticationException {
        if (username == null || username.isBlank() || password == null || password.isEmpty()) {
            throw new AuthenticationException("Username and password are required");
        }
        HttpRequest request = HttpRequest.newBuilder(URI.create(config.tokenUrl()))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(
                        buildTokenRequestBody(config.clientId(), username, password)))
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return extractAccessToken(response);
        } catch (IOException e) {
            throw new AuthenticationException("Keycloak is not reachable at " + config.tokenUrl(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AuthenticationException("Login was interrupted", e);
        }
    }

    private static String extractAccessToken(HttpResponse<String> response) throws AuthenticationException {
        try {
            JSONObject json = new JSONObject(response.body());
            if (response.statusCode() == 200 && json.has("access_token")) {
                return json.getString("access_token");
            }
            String description = json.optString("error_description", json.optString("error", "unknown error"));
            throw new AuthenticationException(
                    "Authentication failed (HTTP " + response.statusCode() + "): " + description);
        } catch (JSONException e) {
            throw new AuthenticationException(
                    "Unexpected reply from Keycloak (HTTP " + response.statusCode() + ")", e);
        }
    }
}
