package com.assignment.reconciliation.api.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

/**
 * API request for starting a reconciliation either from file paths or bundled samples.
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record ReconciliationRunRequest(
        String internalFilePath,
        String providerFilePath,
        Boolean useSampleData
) {
}
