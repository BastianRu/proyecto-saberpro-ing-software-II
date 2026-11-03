package com.unicauca.lisw2t2g08.validacion.api;

import com.unicauca.lisw2t2g08.validacion.pipeline.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * Traduce las excepciones del servicio al contrato de {@code POST /validaciones}:
 *
 * <ul>
 *   <li>{@link ValidationException} (un filtro rechazo el borrador) -&gt; 422.</li>
 *   <li>{@link HttpMessageNotReadableException} (JSON mal formado, cuerpo vacio
 *       o {@code difficulty} desconocido) -&gt; 400.</li>
 *   <li>Cualquier otra excepcion -&gt; 500.</li>
 * </ul>
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    private static final String MALFORMED_REQUEST = "Solicitud mal formada";
    private static final String INTERNAL_ERROR = "Error interno";

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ValidationResponse> handleValidationException(ValidationException exception) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ValidationResponse.invalid(exception.getErrors()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ValidationResponse> handleUnreadableMessage(HttpMessageNotReadableException exception) {
        return ResponseEntity.badRequest()
                .body(ValidationResponse.invalid(List.of(MALFORMED_REQUEST)));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ValidationResponse> handleUnexpectedException(Exception exception) {
        log.error("Error inesperado al validar la pregunta", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ValidationResponse.invalid(List.of(INTERNAL_ERROR)));
    }
}
