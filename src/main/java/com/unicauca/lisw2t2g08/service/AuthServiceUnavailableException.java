package com.unicauca.lisw2t2g08.service;

// Se lanza cuando no se puede contactar con el servicio de autenticacion.

public class AuthServiceUnavailableException extends RuntimeException {
    public AuthServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
