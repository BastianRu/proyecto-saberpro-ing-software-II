package com.unicauca.lisw2t2g08.banco.security;

/**
 * Usuario autenticado extraído de un token válido. El rol es texto porque este
 * servicio no comparte clases con el de Usuarios.
 */
public record AuthenticatedUser(String login, String role, String token) {
}
