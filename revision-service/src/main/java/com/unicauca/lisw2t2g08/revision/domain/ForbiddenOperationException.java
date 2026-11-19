package com.unicauca.lisw2t2g08.revision.domain;

/** El usuario no está autorizado para esta operación (HTTP 403). */
public class ForbiddenOperationException extends RuntimeException {
    public ForbiddenOperationException(String message) {
        super(message);
    }
}
