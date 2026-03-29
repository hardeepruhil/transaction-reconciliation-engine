package com.assignment.reconciliation.api.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.List;

/**
 * Full REST payload returned for a reconciliation run or lookup.
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record ReconciliationResponse(
        List<ReconciliationRecordResponse> matched,
        List<ReconciliationRecordResponse> amountMismatch,
        List<ReconciliationRecordResponse> statusMismatch,
        List<ReconciliationRecordResponse> missingInProvider,
        List<ReconciliationRecordResponse> missingInInternal,
        ReconciliationSummaryResponse summary
) {
}
