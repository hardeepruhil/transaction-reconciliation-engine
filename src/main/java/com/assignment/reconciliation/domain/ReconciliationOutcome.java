package com.assignment.reconciliation.domain;

import java.util.List;

/**
 * Full in-memory reconciliation result before or after persistence.
 */
public record ReconciliationOutcome(
        List<ReconciliationRecord> matched,
        List<ReconciliationRecord> amountMismatch,
        List<ReconciliationRecord> statusMismatch,
        List<ReconciliationRecord> missingInProvider,
        List<ReconciliationRecord> missingInInternal,
        ReconciliationSummary summary
) {
}
