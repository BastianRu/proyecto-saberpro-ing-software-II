package co.edu.unicauca.sistema.desktop;

/** Thrown when Keycloak does not issue a token (wrong credentials, disabled user, unexpected reply). */
public class AuthenticationException extends Exception {

    private static final long serialVersionUID = 1L;

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
