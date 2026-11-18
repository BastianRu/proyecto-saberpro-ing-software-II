package co.edu.unicauca.questionbank.api;

import co.edu.unicauca.questionbank.api.dto.ErrorResponse;
import co.edu.unicauca.questionbank.application.ResourceNotFoundException;
import co.edu.unicauca.questionbank.domain.exception.BusinessRuleException;
import co.edu.unicauca.questionbank.domain.exception.InvalidQuestionException;
import co.edu.unicauca.questionbank.domain.exception.InvalidStatusTransitionException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

/** Translates domain and application exceptions into HTTP responses. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * @param ex missing resource
     * @return 404 Not Found
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> notFound(ResourceNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), List.of());
    }

    /**
     * @param ex invalid question content
     * @return 400 Bad Request with every broken rule
     */
    @ExceptionHandler(InvalidQuestionException.class)
    public ResponseEntity<ErrorResponse> invalidQuestion(InvalidQuestionException ex) {
        return build(HttpStatus.BAD_REQUEST, "The question is not valid", ex.getErrors());
    }

    /**
     * @param ex forbidden lifecycle transition
     * @return 409 Conflict
     */
    @ExceptionHandler(InvalidStatusTransitionException.class)
    public ResponseEntity<ErrorResponse> invalidTransition(InvalidStatusTransitionException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), List.of());
    }

    /**
     * @param ex other business rule violation
     * @return 422 Unprocessable Entity
     */
    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ErrorResponse> businessRule(BusinessRuleException ex) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), List.of());
    }

    /**
     * @param ex constraint violated in the database
     * @return 409 Conflict
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> integrity(DataIntegrityViolationException ex) {
        return build(HttpStatus.CONFLICT, "The operation violates a database constraint", List.of());
    }

    /**
     * @param ex invalid argument
     * @return 400 Bad Request
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> badRequest(IllegalArgumentException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), List.of());
    }

    private static ResponseEntity<ErrorResponse> build(HttpStatus status, String message, List<String> details) {
        ErrorResponse body = new ErrorResponse(status.value(), status.getReasonPhrase(), message, details,
                LocalDateTime.now());
        return ResponseEntity.status(status).body(body);
    }
}
