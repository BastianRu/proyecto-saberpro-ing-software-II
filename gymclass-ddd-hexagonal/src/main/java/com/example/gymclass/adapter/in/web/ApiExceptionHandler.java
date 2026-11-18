package com.example.gymclass.adapter.in.web;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.NoSuchElementException;

/** Converts application and domain exceptions into consistent HTTP error responses. */
@RestControllerAdvice
public class ApiExceptionHandler {
    /** Handles missing resources. */
    @ExceptionHandler(NoSuchElementException.class) @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String,String> handleNotFound(Exception ex) { return Map.of("error", ex.getMessage()); }
    /** Handles violated business rules and invalid domain input. */
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class}) @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String,String> handleBusinessRule(Exception ex) { return Map.of("error", ex.getMessage()); }
}
