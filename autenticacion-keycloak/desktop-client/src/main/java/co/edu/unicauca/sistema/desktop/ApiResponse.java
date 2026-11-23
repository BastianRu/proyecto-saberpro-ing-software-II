package co.edu.unicauca.sistema.desktop;

/**
 * Result of a call to the API Gateway.
 *
 * @param status HTTP status code
 * @param body   response body (may be empty)
 */
public record ApiResponse(int status, String body) {

    public boolean isSuccess() {
        return status >= 200 && status < 300;
    }

    /** Message shown in the UI. Maps the security-relevant statuses to an explanation for the user. */
    public String describe() {
        return switch (status) {
            case 401 -> "401 Unauthorized: missing, invalid or expired token. Log in again.";
            case 403 -> "403 Forbidden: your role is not allowed to use this endpoint.";
            case 502, 503, 504 -> status + " The gateway could not reach the microservice.";
            default -> isSuccess()
                    ? status + " OK: " + body
                    : status + " Unexpected response: " + body;
        };
    }
}
