package com.assignment.reconciliation.domain;

/**
 * Explains why a transaction landed in a specific reconciliation bucket.
 */
public enum ComparisonReason {
    EXACT_MATCH,
    AMOUNT_MISMATCH,
    STATUS_MISMATCH,
    TIMESTAMP_OUTSIDE_TOLERANCE,
    MISSING_IN_PROVIDER,
    MISSING_IN_INTERNAL
}
