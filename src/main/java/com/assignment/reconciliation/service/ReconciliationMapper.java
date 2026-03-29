package com.assignment.reconciliation.service;

import com.assignment.reconciliation.api.dto.ReconciliationRecordResponse;
import com.assignment.reconciliation.api.dto.ReconciliationResponse;
import com.assignment.reconciliation.api.dto.ReconciliationSummaryResponse;
import com.assignment.reconciliation.domain.ReconciliationCategory;
import com.assignment.reconciliation.domain.ReconciliationOutcome;
import com.assignment.reconciliation.domain.ReconciliationRecord;
import com.assignment.reconciliation.domain.ReconciliationSummary;
import com.assignment.reconciliation.exception.ResourceNotFoundException;
import com.assignment.reconciliation.persistence.entity.ReconciliationJobEntity;
import com.assignment.reconciliation.persistence.entity.ReconciliationResultEntity;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * Converts between internal domain/persistence models and the API response contract.
 */
@Component
public class ReconciliationMapper {

    public ReconciliationResponse toResponse(ReconciliationOutcome outcome) {
        return new ReconciliationResponse(
                mapRecords(outcome.matched()),
                mapRecords(outcome.amountMismatch()),
                mapRecords(outcome.statusMismatch()),
                mapRecords(outcome.missingInProvider()),
                mapRecords(outcome.missingInInternal()),
                toSummaryResponse(outcome.summary())
        );
    }

    public ReconciliationResponse fromEntity(ReconciliationJobEntity entity) {
        if (entity == null) {
            throw new ResourceNotFoundException("Reconciliation job not found");
        }

        // Persisted rows are sorted before mapping so API responses remain deterministic.
        List<ReconciliationResultEntity> sorted = entity.getResults().stream()
                .sorted(Comparator.comparing(ReconciliationResultEntity::getTxnId))
                .toList();

        return new ReconciliationResponse(
                mapEntityRecords(sorted, ReconciliationCategory.MATCHED),
                mapEntityRecords(sorted, ReconciliationCategory.AMOUNT_MISMATCH),
                mapEntityRecords(sorted, ReconciliationCategory.STATUS_MISMATCH),
                mapEntityRecords(sorted, ReconciliationCategory.MISSING_IN_PROVIDER),
                mapEntityRecords(sorted, ReconciliationCategory.MISSING_IN_INTERNAL),
                new ReconciliationSummaryResponse(
                        entity.getId(),
                        entity.getTotalInternal(),
                        entity.getTotalProvider(),
                        entity.getMatchedCount(),
                        entity.getAmountMismatchCount(),
                        entity.getStatusMismatchCount(),
                        entity.getMissingInProviderCount(),
                        entity.getMissingInInternalCount(),
                        entity.getTimestampToleranceSeconds()
                )
        );
    }

    private List<ReconciliationRecordResponse> mapRecords(List<ReconciliationRecord> records) {
        return records.stream()
                .map(record -> new ReconciliationRecordResponse(
                        record.txnId(),
                        record.internalAmount(),
                        record.providerAmount(),
                        record.internalStatus(),
                        record.providerStatus(),
                        record.internalTimestamp(),
                        record.providerTimestamp(),
                        record.timestampDifferenceSeconds(),
                        record.reason()
                ))
                .toList();
    }

    private List<ReconciliationRecordResponse> mapEntityRecords(
            List<ReconciliationResultEntity> records,
            ReconciliationCategory category
    ) {
        return records.stream()
                .filter(record -> record.getCategory() == category)
                .map(record -> new ReconciliationRecordResponse(
                        record.getTxnId(),
                        record.getInternalAmount(),
                        record.getProviderAmount(),
                        record.getInternalStatus(),
                        record.getProviderStatus(),
                        record.getInternalTimestamp(),
                        record.getProviderTimestamp(),
                        record.getTimestampDifferenceSeconds(),
                        record.getReason()
                ))
                .toList();
    }

    private ReconciliationSummaryResponse toSummaryResponse(ReconciliationSummary summary) {
        return new ReconciliationSummaryResponse(
                summary.jobId(),
                summary.totalInternal(),
                summary.totalProvider(),
                summary.matchedCount(),
                summary.amountMismatchCount(),
                summary.statusMismatchCount(),
                summary.missingInProviderCount(),
                summary.missingInInternalCount(),
                summary.timestampToleranceSeconds()
        );
    }
}
