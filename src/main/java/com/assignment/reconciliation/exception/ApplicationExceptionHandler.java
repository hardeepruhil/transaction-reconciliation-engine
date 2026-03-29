package com.assignment.reconciliation.exception;

import com.assignment.reconciliation.api.dto.ValidationErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * Converts internal exceptions into stable JSON error responses for the client.
 */
@RestControllerAdvice
public class ApplicationExceptionHandler {

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidation(ValidationException exception) {
        return ResponseEntity.badRequest()
                .body(new ValidationErrorResponse(exception.getMessage(), exception.getErrors()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ValidationErrorResponse> handleNotFound(ResourceNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ValidationErrorResponse(exception.getMessage(), List.of()));
    }
}
