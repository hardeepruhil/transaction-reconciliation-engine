package com.assignment.reconciliation.exception;

import java.util.List;

/**
 * Raised when an input file or record violates the expected reconciliation contract.
 */
public class ValidationException extends RuntimeException {

    private final List<String> errors;

    public ValidationException(String message, List<String> errors) {
        super(message);
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }
}
