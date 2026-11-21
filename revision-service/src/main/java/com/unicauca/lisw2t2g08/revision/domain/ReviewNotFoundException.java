package com.unicauca.lisw2t2g08.revision.domain;

/** La revisión no existe (HTTP 404). */
public class ReviewNotFoundException extends RuntimeException {
    public ReviewNotFoundException(String message) {
        super(message);
    }
}
