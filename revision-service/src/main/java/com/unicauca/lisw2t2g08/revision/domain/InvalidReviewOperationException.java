package com.unicauca.lisw2t2g08.revision.domain;

/** La operación no es válida en el estado actual de la revisión (HTTP 409). */
public class InvalidReviewOperationException extends RuntimeException {
    public InvalidReviewOperationException(String message) {
        super(message);
    }
}
