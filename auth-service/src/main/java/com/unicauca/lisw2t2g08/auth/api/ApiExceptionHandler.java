package com.unicauca.lisw2t2g08.auth.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.unicauca.lisw2t2g08.auth.service.AuthenticationException;
import com.unicauca.lisw2t2g08.auth.service.InvalidPasswordException;
import com.unicauca.lisw2t2g08.auth.service.UserAlreadyExistsException;

// Traduce las excepciones del servicio a respuestas HTTP

@RestControllerAdvice
public class ApiExceptionHandler {

    public record ErrorResponse(String message, List<String> errors) { }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> authentication(AuthenticationException e) {
        return build(HttpStatus.UNAUTHORIZED, e.getMessage(), List.of());
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> alreadyExists(UserAlreadyExistsException e) {
        return build(HttpStatus.CONFLICT, e.getMessage(), List.of());
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ErrorResponse> invalidPassword(InvalidPasswordException e) {
        return build(HttpStatus.BAD_REQUEST, e.getMessage(), e.getErrors());
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
