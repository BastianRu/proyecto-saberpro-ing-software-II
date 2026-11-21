package com.unicauca.lisw2t2g08.domain;

// Sesion del usuario autenticado en el cliente. Reemplaza al User en la interfaz:
// el cliente ya no conoce la contraseña ni el resto del registro, solo quien es,
// su rol y el token que debe presentar a los servicios.

public final class Session {

    private final String login;
    private final Role role;
    private final String token;

    public Session(String login, Role role, String token) {
        if (login == null || login.isBlank()) {
            throw new IllegalArgumentException("El login de la sesión es obligatorio");
        }
        if (role == null) {
            throw new IllegalArgumentException("El rol de la sesión es obligatorio");
        }
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("El token de la sesión es obligatorio");
        }
        this.login = login;
        this.role = role;
        this.token = token;
    }

    public String getLogin() {
        return login;
    }

    public Role getRole() {
        return role;
    }

    // Se envia como "Authorization: Bearer <token>" a los demas servicios
    public String getToken() {
        return token;
    }

    @Override
    public String toString() {
        return "Session{login='" + login + "', role=" + role + '}'; // sin el token
    }
}
