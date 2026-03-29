package com.assignment.reconciliation.service;

import com.assignment.reconciliation.domain.NormalizedTransaction;

import java.util.List;

/**
 * Simple wrapper so validation can return both normalized records and user-facing errors.
 */
public record ValidationResult(
        List<NormalizedTransaction> transactions,
        List<String> errors
) {
}
