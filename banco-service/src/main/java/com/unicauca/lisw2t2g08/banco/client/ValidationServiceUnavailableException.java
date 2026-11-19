package com.unicauca.lisw2t2g08.banco.client;

/**
 * Se lanza cuando no es posible comunicarse con validacion-service: el
 * servicio esta caido, no responde dentro del tiempo de espera o la peticion
 * fue interrumpida.
 *
 * Es distinta de {@link ValidationException}: aquella significa que el
 * servicio respondio y una regla de negocio rechazo el borrador (HTTP 422);
 * esta significa que la validacion no se pudo ejecutar (HTTP 503).
 */
public class ValidationServiceUnavailableException extends RuntimeException {

    public ValidationServiceUnavailableException(String message) {
        super(message);
    }

    public ValidationServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
