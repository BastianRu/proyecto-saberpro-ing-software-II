package com.unicauca.lisw2t2g08.banco.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.unicauca.lisw2t2g08.banco.domain.InvalidStateOperationException;
import com.unicauca.lisw2t2g08.banco.pipeline.ValidationException;
import com.unicauca.lisw2t2g08.banco.service.QuestionNotFoundException;

/** Traduce las excepciones del dominio a respuestas HTTP. */
@RestControllerAdvice
public class ApiExceptionHandler {

    public record ErrorResponse(String message, List<String> errors) { }

    // 422: la solicitud está bien formada pero el borrador incumple las reglas estructurales (RF-05, RF-08 a RF-13)
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> validation(ValidationException e) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, "La pregunta no supera la validación estructural", e.getErrors());
    }

    @ExceptionHandler(QuestionNotFoundException.class)
    public ResponseEntity<ErrorResponse> notFound(QuestionNotFoundException e) {
        return build(HttpStatus.NOT_FOUND, e.getMessage(), List.of());
    }

    @ExceptionHandler(InvalidStateOperationException.class)
    public ResponseEntity<ErrorResponse> conflict(InvalidStateOperationException e) {
        return build(HttpStatus.CONFLICT, e.getMessage(), List.of());
    }

    @ExceptionHandler({IllegalArgumentException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ErrorResponse> badRequest(Exception e) {
        String message = e instanceof IllegalArgumentException ? e.getMessage() : "Solicitud mal formada";
        return build(HttpStatus.BAD_REQUEST, message, List.of());
    }

    private static ResponseEntity<ErrorResponse> build(HttpStatus status, String message, List<String> errors) {
        return ResponseEntity.status(status).body(new ErrorResponse(message, errors));
    }
}
