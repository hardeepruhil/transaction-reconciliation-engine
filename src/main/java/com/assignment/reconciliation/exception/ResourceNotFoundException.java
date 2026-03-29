package com.assignment.reconciliation.exception;

/**
 * Raised when a requested reconciliation job cannot be found in storage.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
