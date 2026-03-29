package com.assignment.reconciliation.domain;

/**
 * Top-level output buckets required by the assignment.
 */
public enum ReconciliationCategory {
    MATCHED,
    AMOUNT_MISMATCH,
    STATUS_MISMATCH,
    MISSING_IN_PROVIDER,
    MISSING_IN_INTERNAL
}
