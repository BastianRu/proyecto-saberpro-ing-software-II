package co.edu.unicauca.questionbank.assessment.adapter.in.web;

import co.edu.unicauca.questionbank.assessment.application.AssessmentNotFoundException;
import co.edu.unicauca.questionbank.assessment.domain.exception.AssessmentRuleException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Translates the exceptions of the assessment core into HTTP responses. It only
 * applies to {@link MockExamController} and uses the same JSON error format as
 * the rest of the API.
 */
@RestControllerAdvice(assignableTypes = MockExamController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AssessmentExceptionHandler {

    /**
     * JSON error body.
     *
     * @param status    HTTP status code
     * @param error     short error name
     * @param message   description
     * @param details   individual errors
     * @param timestamp moment of the error
     */
    public record ApiError(int status, String error, String message, List<String> details,
                           LocalDateTime timestamp) {
    }

    /**
     * @param ex missing attempt, quiz or student
     * @return 404 Not Found
     */
    @ExceptionHandler(AssessmentNotFoundException.class)
    public ResponseEntity<ApiError> notFound(AssessmentNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), List.of());
    }

    /**
     * @param ex violated assessment rule
     * @return 422 Unprocessable Entity
     */
    @ExceptionHandler(AssessmentRuleException.class)
    public ResponseEntity<ApiError> rule(AssessmentRuleException ex) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), List.of());
    }

    /**
     * @param ex invalid input (for example, a malformed attempt id)
     * @return 400 Bad Request
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> badRequest(IllegalArgumentException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), List.of());
    }

    /**
     * @param ex invalid JSON body
     * @return 400 Bad Request with every invalid field
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> invalidBody(MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage()).toList();
        return build(HttpStatus.BAD_REQUEST, "The request body is not valid", details);
    }

    private static ResponseEntity<ApiError> build(HttpStatus status, String message, List<String> details) {
        return ResponseEntity.status(status)
                .body(new ApiError(status.value(), status.getReasonPhrase(), message, details, LocalDateTime.now()));
    }
}
