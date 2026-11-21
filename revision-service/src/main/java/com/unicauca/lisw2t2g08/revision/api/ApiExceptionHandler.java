package com.unicauca.lisw2t2g08.revision.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.unicauca.lisw2t2g08.revision.domain.ForbiddenOperationException;
import com.unicauca.lisw2t2g08.revision.domain.InvalidReviewOperationException;
import com.unicauca.lisw2t2g08.revision.domain.ReviewNotFoundException;
import com.unicauca.lisw2t2g08.revision.service.BancoRejectedException;
import com.unicauca.lisw2t2g08.revision.service.BancoUnavailableException;

/** Traduce las excepciones del dominio a respuestas HTTP. */
@RestControllerAdvice
public class ApiExceptionHandler {

    public record ErrorResponse(String message, List<String> errors) { }

    @ExceptionHandler(ReviewNotFoundException.class)
    public ResponseEntity<ErrorResponse> notFound(ReviewNotFoundException e) {
        return build(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(ForbiddenOperationException.class)
    public ResponseEntity<ErrorResponse> forbidden(ForbiddenOperationException e) {
        return build(HttpStatus.FORBIDDEN, e.getMessage());
    }

    // 409: el recurso no está en un estado que permita la operación (revisión o pregunta del Banco)
    @ExceptionHandler({InvalidReviewOperationException.class, BancoRejectedException.class})
    public ResponseEntity<ErrorResponse> conflict(RuntimeException e) {
        return build(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(BancoUnavailableException.class)
    public ResponseEntity<ErrorResponse> unavailable(BancoUnavailableException e) {
        return build(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage());
    }

    @ExceptionHandler({IllegalArgumentException.class, HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErrorResponse> badRequest(Exception e) {
        String message = e instanceof IllegalArgumentException ? e.getMessage() : "Solicitud mal formada";
        return build(HttpStatus.BAD_REQUEST, message);
    }

    private static ResponseEntity<ErrorResponse> build(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new ErrorResponse(message, List.of()));
    }
}
