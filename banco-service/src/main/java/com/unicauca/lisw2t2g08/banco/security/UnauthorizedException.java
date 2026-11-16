package com.unicauca.lisw2t2g08.banco.security;

/**
 * Lanzada cuando la peticion no incluye un token valido o el token ha expirado.
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
