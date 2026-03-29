package com.assignment.reconciliation.service;

import com.assignment.reconciliation.domain.ReconciliationCategory;
import com.assignment.reconciliation.domain.ReconciliationOutcome;
import com.assignment.reconciliation.domain.ReconciliationRecord;
import com.assignment.reconciliation.domain.RunStatus;
import com.assignment.reconciliation.persistence.entity.ReconciliationJobEntity;
import com.assignment.reconciliation.persistence.entity.ReconciliationResultEntity;
import com.assignment.reconciliation.persistence.repository.ReconciliationJobRepository;
import com.assignment.reconciliation.persistence.repository.ReconciliationResultRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ReconciliationPersistenceService {

    private final ReconciliationJobRepository jobRepository;
    private final ReconciliationResultRepository resultRepository;

    public ReconciliationPersistenceService(
            ReconciliationJobRepository jobRepository,
            ReconciliationResultRepository resultRepository
    ) {
        this.jobRepository = jobRepository;
        this.resultRepository = resultRepository;
    }

    @Transactional
    public ReconciliationJobEntity saveNewRun(
            ReconciliationOutcome outcome,
            String idempotencyKey,
            String internalHash,
            String providerHash
    ) {
        // Job metadata stores the "why and when" of a run, while result rows store the
        // category-by-category breakdown that we later expose through the API.
        ReconciliationJobEntity job = new ReconciliationJobEntity();
        job.setId(UUID.randomUUID().toString());
        job.setIdempotencyKey(idempotencyKey);
        job.setStatus(RunStatus.COMPLETED);
        job.setCreatedAt(Instant.now());
        job.setTimestampToleranceSeconds(outcome.summary().timestampToleranceSeconds());
        job.setInternalHash(internalHash);
        job.setProviderHash(providerHash);
        job.setTotalInternal(outcome.summary().totalInternal());
        job.setTotalProvider(outcome.summary().totalProvider());
        job.setMatchedCount(outcome.summary().matchedCount());
        job.setAmountMismatchCount(outcome.summary().amountMismatchCount());
        job.setStatusMismatchCount(outcome.summary().statusMismatchCount());
        job.setMissingInProviderCount(outcome.summary().missingInProviderCount());
        job.setMissingInInternalCount(outcome.summary().missingInInternalCount());
        jobRepository.save(job);

        List<ReconciliationResultEntity> resultEntities = new ArrayList<>();
        addRecords(resultEntities, job, outcome.matched(), ReconciliationCategory.MATCHED);
        addRecords(resultEntities, job, outcome.amountMismatch(), ReconciliationCategory.AMOUNT_MISMATCH);
        addRecords(resultEntities, job, outcome.statusMismatch(), ReconciliationCategory.STATUS_MISMATCH);
        addRecords(resultEntities, job, outcome.missingInProvider(), ReconciliationCategory.MISSING_IN_PROVIDER);
        addRecords(resultEntities, job, outcome.missingInInternal(), ReconciliationCategory.MISSING_IN_INTERNAL);
        resultRepository.saveAll(resultEntities);

        return jobRepository.findDetailedById(job.getId()).orElseThrow();
    }

    @Transactional(readOnly = true)
    public ReconciliationJobEntity findExistingByIdempotencyKey(String idempotencyKey) {
        // Used to implement idempotent reruns when the same logical job is submitted again.
        return jobRepository.findDetailedByIdempotencyKey(idempotencyKey).orElse(null);
    }

    @Transactional(readOnly = true)
    public ReconciliationJobEntity getJobById(String jobId) {
        return jobRepository.findDetailedById(jobId).orElse(null);
    }

    private void addRecords(
            List<ReconciliationResultEntity> target,
            ReconciliationJobEntity job,
            List<ReconciliationRecord> records,
            ReconciliationCategory category
    ) {
        for (ReconciliationRecord record : records) {
            ReconciliationResultEntity entity = new ReconciliationResultEntity();
            entity.setJob(job);
            entity.setCategory(category);
            entity.setReason(record.reason());
            entity.setTxnId(record.txnId());
            entity.setInternalAmount(record.internalAmount());
            entity.setProviderAmount(record.providerAmount());
            entity.setInternalStatus(record.internalStatus());
            entity.setProviderStatus(record.providerStatus());
            entity.setInternalTimestamp(record.internalTimestamp());
            entity.setProviderTimestamp(record.providerTimestamp());
            entity.setTimestampDifferenceSeconds(record.timestampDifferenceSeconds());
            target.add(entity);
        }
    }
}
