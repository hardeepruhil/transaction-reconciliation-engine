package com.assignment.reconciliation.domain;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Detailed comparison output for a single transaction ID across both systems.
 */
public record ReconciliationRecord(
        String txnId,
        BigDecimal internalAmount,
        BigDecimal providerAmount,
        String internalStatus,
        String providerStatus,
        Instant internalTimestamp,
        Instant providerTimestamp,
        long timestampDifferenceSeconds,
        ComparisonReason reason
) {
}
