package com.unicauca.lisw2t2g08.pipeline;

import java.util.List;

/**
 * Se lanza cuando un filtro detecta que la pregunta incumple alguna regla
 * estructural de HE 3. Acumula todos los errores del filtro que la lanza para
 * que la interfaz pueda mostrarlos de una vez.
 */
public class ValidationException extends RuntimeException {

    private final List<String> errors;

    public ValidationException(List<String> errors) {
        super(String.join("; ", errors));
        this.errors = List.copyOf(errors);
    }

    public List<String> getErrors() {
        return errors;
    }
}
