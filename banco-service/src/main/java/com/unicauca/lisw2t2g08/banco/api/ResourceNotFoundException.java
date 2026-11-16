package com.unicauca.lisw2t2g08.banco.api;

/**
 * Excepcion para recursos no encontrados (HTTP 404).
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
