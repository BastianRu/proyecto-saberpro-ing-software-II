package com.unicauca.lisw2t2g08.revision.service;

/** El Banco respondió pero rechazó la operación. La API la traduce a 409 con el mensaje del Banco. */
public class BancoRejectedException extends RuntimeException {
    public BancoRejectedException(String message) {
        super(message);
    }
}
