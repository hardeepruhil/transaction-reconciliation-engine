package com.assignment.reconciliation.domain;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Canonical in-memory representation used by the reconciliation engine after validation.
 */
public record NormalizedTransaction(
        String txnId,
        BigDecimal amount,
        String status,
        Instant timestamp
) {
}
