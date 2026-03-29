package com.assignment.reconciliation.api.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

/**
 * API-facing summary counts for a reconciliation job.
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record ReconciliationSummaryResponse(
        String jobId,
        int totalInternal,
        int totalProvider,
        int matchedCount,
        int amountMismatchCount,
        int statusMismatchCount,
        int missingInProviderCount,
        int missingInInternalCount,
        long timestampToleranceSeconds
) {
}
