package com.unicauca.lisw2t2g08.validacion.api;

import java.util.List;

/**
 * Respuesta uniforme de {@code POST /validaciones}: indica si el borrador es
 * valido y, cuando no lo es, la lista de mensajes producidos por los filtros.
 */
public record ValidationResponse(boolean valid, List<String> errors) {

    /** Respuesta para un borrador que atraveso el pipeline sin errores. */
    public static ValidationResponse success() {
        return new ValidationResponse(true, List.of());
    }

    /** Respuesta para un borrador rechazado o una peticion mal formada. */
    public static ValidationResponse invalid(List<String> errors) {
        return new ValidationResponse(false, List.copyOf(errors));
    }
}
