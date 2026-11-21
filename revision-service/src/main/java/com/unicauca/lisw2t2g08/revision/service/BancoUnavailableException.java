package com.unicauca.lisw2t2g08.revision.service;

/** No se pudo contactar al Banco o respondió algo inesperado. La API responde 503. */
public class BancoUnavailableException extends RuntimeException {
    public BancoUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
