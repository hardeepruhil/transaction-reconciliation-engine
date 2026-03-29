package com.assignment.reconciliation.domain;

/**
 * Aggregated counts returned with each reconciliation response and stored with the job.
 */
public record ReconciliationSummary(
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
