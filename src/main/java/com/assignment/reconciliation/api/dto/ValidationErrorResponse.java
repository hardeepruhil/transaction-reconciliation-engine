package com.assignment.reconciliation.api.dto;

import java.util.List;

/**
 * Standard error shape for validation and lookup failures.
 */
public record ValidationErrorResponse(
        String message,
        List<String> errors
) {
}
