package com.assignment.reconciliation.service;

import com.assignment.reconciliation.api.dto.ReconciliationResponse;
import com.assignment.reconciliation.api.dto.ReconciliationSummaryResponse;
import com.assignment.reconciliation.config.ReconciliationProperties;
import com.assignment.reconciliation.domain.ReconciliationOutcome;
import com.assignment.reconciliation.domain.ReconciliationSummary;
import com.assignment.reconciliation.exception.ResourceNotFoundException;
import com.assignment.reconciliation.exception.ValidationException;
import com.assignment.reconciliation.persistence.entity.ReconciliationJobEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Orchestrates ingestion, idempotency, reconciliation, persistence, and API response mapping.
 */
@Service
public class ReconciliationService {

    private final ReconciliationProperties properties;
    private final TransactionIngestionService ingestionService;
    private final ReconciliationEngine reconciliationEngine;
    private final HashingService hashingService;
    private final ReconciliationPersistenceService persistenceService;
    private final ReconciliationMapper reconciliationMapper;

    public ReconciliationService(
            ReconciliationProperties properties,
            TransactionIngestionService ingestionService,
            ReconciliationEngine reconciliationEngine,
            HashingService hashingService,
            ReconciliationPersistenceService persistenceService,
            ReconciliationMapper reconciliationMapper
    ) {
        this.properties = properties;
        this.ingestionService = ingestionService;
        this.reconciliationEngine = reconciliationEngine;
        this.hashingService = hashingService;
        this.persistenceService = persistenceService;
        this.reconciliationMapper = reconciliationMapper;
    }

    @Transactional
    public ReconciliationResponse run(ReconciliationRequest request) {
        String internalPath = request.useSampleData() ? properties.sampleInternalPath() : request.internalPath();
        String providerPath = request.useSampleData() ? properties.sampleProviderPath() : request.providerPath();

        if (internalPath == null || internalPath.isBlank() || providerPath == null || providerPath.isBlank()) {
            throw new ValidationException(
                    "Both datasets are required",
                    List.of("Provide both internal and provider file paths, or set useSampleData=true.")
            );
        }

        var internalTransactions = ingestionService.loadTransactions(internalPath, "internal");
        var providerTransactions = ingestionService.loadTransactions(providerPath, "provider");

        String internalHash = hashingService.hashTransactions(internalTransactions);
        String providerHash = hashingService.hashTransactions(providerTransactions);
        String idempotencyKey = hashingService.buildIdempotencyKey(
                internalHash,
                providerHash,
                properties.timestampToleranceSeconds()
        );

        ReconciliationJobEntity existingJob = persistenceService.findExistingByIdempotencyKey(idempotencyKey);
        if (existingJob != null) {
            // Re-running the same effective input returns the stored result instead of
            // generating duplicate rows, which makes the API idempotent.
            return reconciliationMapper.fromEntity(existingJob);
        }

        ReconciliationOutcome provisionalOutcome = reconciliationEngine.reconcile(
                "pending",
                internalTransactions,
                providerTransactions,
                properties.timestampToleranceSeconds()
        );

        ReconciliationJobEntity savedJob = persistenceService.saveNewRun(
                provisionalOutcome,
                idempotencyKey,
                internalHash,
                providerHash
        );
        // For the immediate response we reuse the already-computed outcome and simply inject
        // the real persisted job id, avoiding an unnecessary second reconciliation pass.
        return reconciliationMapper.toResponse(withPersistedJobId(provisionalOutcome, savedJob.getId()));
    }

    @Transactional(readOnly = true)
    public ReconciliationResponse getByJobId(String jobId) {
        ReconciliationJobEntity entity = persistenceService.getJobById(jobId);
        if (entity == null) {
            throw new ResourceNotFoundException("Reconciliation job '" + jobId + "' not found");
        }
        return reconciliationMapper.fromEntity(entity);
    }

    @Transactional(readOnly = true)
    public ReconciliationSummaryResponse getSummary(String jobId) {
        ReconciliationJobEntity entity = persistenceService.getJobById(jobId);
        if (entity == null) {
            throw new ResourceNotFoundException("Reconciliation job '" + jobId + "' not found");
        }
        return reconciliationMapper.fromEntity(entity).summary();
    }

    private ReconciliationOutcome withPersistedJobId(ReconciliationOutcome outcome, String jobId) {
        return new ReconciliationOutcome(
                outcome.matched(),
                outcome.amountMismatch(),
                outcome.statusMismatch(),
                outcome.missingInProvider(),
                outcome.missingInInternal(),
                new ReconciliationSummary(
                        jobId,
                        outcome.summary().totalInternal(),
                        outcome.summary().totalProvider(),
                        outcome.summary().matchedCount(),
                        outcome.summary().amountMismatchCount(),
                        outcome.summary().statusMismatchCount(),
                        outcome.summary().missingInProviderCount(),
                        outcome.summary().missingInInternalCount(),
                        outcome.summary().timestampToleranceSeconds()
                )
        );
    }
}
