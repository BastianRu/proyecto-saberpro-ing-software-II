package co.edu.unicauca.sistema.desktop;

/**
 * Endpoints and OAuth2 client identifier used by the desktop client.
 *
 * @param tokenUrl   Keycloak token endpoint of the realm {@code sistema}
 * @param gatewayUrl base URL of the API Gateway (without trailing slash)
 * @param clientId   Keycloak client that has "Direct access grants" enabled
 */
public record ClientConfig(String tokenUrl, String gatewayUrl, String clientId) {

    /** Defaults for the local setup of the workshop; each value can be overridden with a system property. */
    public static ClientConfig fromSystemProperties() {
        return new ClientConfig(
                System.getProperty("keycloak.token.url",
                        "http://localhost:8080/realms/sistema/protocol/openid-connect/token"),
                System.getProperty("gateway.url", "http://localhost:8081"),
                System.getProperty("keycloak.client.id", "sistema-desktop"));
    }
}
