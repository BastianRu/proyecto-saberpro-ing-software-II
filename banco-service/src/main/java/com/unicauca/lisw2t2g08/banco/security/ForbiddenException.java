package com.unicauca.lisw2t2g08.banco.security;

/** El usuario está autenticado pero su rol no permite la operación. La API responde 403. */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
