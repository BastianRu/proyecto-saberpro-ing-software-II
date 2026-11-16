package com.unicauca.lisw2t2g08.banco.client;

import java.util.Collections;
import java.util.List;

/**
 * Excepcion lanzada cuando una pregunta o borrador falla las reglas de
 * validacion estructural (HU03).
 */
public class ValidationException extends RuntimeException {

    private final List<String> errors;

    public ValidationException(List<String> errors) {
        super(errors == null || errors.isEmpty() ? "Error de validacion" : String.join("; ", errors));
        this.errors = errors == null ? List.of() : List.copyOf(errors);
    }

    public List<String> getErrors() {
        return Collections.unmodifiableList(errors);
    }
}
