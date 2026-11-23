package co.edu.unicauca.sistema.desktop;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Calls the protected endpoints through the API Gateway, attaching the JWT as a
 * {@code Authorization: Bearer <token>} header (RFC 6750).
 */
public class GatewayClient {

    private final ClientConfig config;
    private final HttpClient httpClient;

    public GatewayClient(ClientConfig config) {
        this.config = config;
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    }

    /**
     * Performs a GET on {@code <gateway><path>}.
     *
     * @param token access token, or {@code null}/blank to send the request without credentials
     * @param path  path starting with {@code /}, for example {@code /a/hello_student}
     * @throws IOException if the gateway cannot be reached
     */
    public ApiResponse get(String token, String path) throws IOException {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(config.gatewayUrl() + path))
                .timeout(Duration.ofSeconds(10))
                .GET();
        if (token != null && !token.isBlank()) {
            builder.header("Authorization", "Bearer " + token);
        }
        try {
            HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            return new ApiResponse(response.statusCode(), response.body());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Request interrupted", e);
        }
    }
}
