package com.unicauca.lisw2t2g08.banco.domain;

/**
 * Se lanza cuando se pide a una pregunta una operación que su estado actual no
 * permite (transición inválida o edición fuera de Borrador). La capa REST la
 * traduce a 409 Conflict.
 */
public class InvalidStateOperationException extends IllegalStateException {

    public InvalidStateOperationException(String message) {
        super(message);
    }
}
