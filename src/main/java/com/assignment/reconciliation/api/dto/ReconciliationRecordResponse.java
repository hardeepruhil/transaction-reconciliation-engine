package com.assignment.reconciliation.api.dto;

import com.assignment.reconciliation.domain.ComparisonReason;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * API-facing version of a single reconciliation record.
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record ReconciliationRecordResponse(
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
