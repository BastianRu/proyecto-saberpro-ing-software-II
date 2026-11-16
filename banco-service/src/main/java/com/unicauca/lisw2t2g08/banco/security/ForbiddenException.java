package com.unicauca.lisw2t2g08.banco.security;

/**
 * Lanzada cuando el rol del usuario autenticado no cuenta con permisos suficientes.
 */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
