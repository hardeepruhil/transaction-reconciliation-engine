package com.assignment.reconciliation.service;

import com.assignment.reconciliation.domain.ComparisonReason;
import com.assignment.reconciliation.domain.NormalizedTransaction;
import com.assignment.reconciliation.domain.ReconciliationCategory;
import com.assignment.reconciliation.domain.ReconciliationOutcome;
import com.assignment.reconciliation.domain.ReconciliationRecord;
import com.assignment.reconciliation.domain.ReconciliationSummary;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ReconciliationEngine {

    public ReconciliationOutcome reconcile(
            String jobId,
            List<NormalizedTransaction> internalTransactions,
            List<NormalizedTransaction> providerTransactions,
            long toleranceSeconds
    ) {
        // We index both datasets by txn_id so reconciliation stays close to O(n + m)
        // instead of degrading into nested-loop comparisons.
        Map<String, NormalizedTransaction> internalByTxnId = indexByTxnId(internalTransactions);
        Map<String, NormalizedTransaction> providerByTxnId = indexByTxnId(providerTransactions);

        List<ReconciliationRecord> matched = new ArrayList<>();
        List<ReconciliationRecord> amountMismatch = new ArrayList<>();
        List<ReconciliationRecord> statusMismatch = new ArrayList<>();
        List<ReconciliationRecord> missingInProvider = new ArrayList<>();
        List<ReconciliationRecord> missingInInternal = new ArrayList<>();

        for (NormalizedTransaction internal : internalByTxnId.values()) {
            NormalizedTransaction provider = providerByTxnId.remove(internal.txnId());
            if (provider == null) {
                missingInProvider.add(toRecord(internal, null, 0, ComparisonReason.MISSING_IN_PROVIDER));
                continue;
            }

            long timestampDifference = Math.abs(Duration.between(internal.timestamp(), provider.timestamp()).getSeconds());
            boolean amountMatches = internal.amount().compareTo(provider.amount()) == 0;
            boolean statusMatches = internal.status().equals(provider.status());
            boolean timestampMatches = timestampDifference <= toleranceSeconds;

            // The assignment asks for a fixed set of output buckets. Timestamp-only misses are
            // therefore stored in the status_mismatch bucket, but with an explicit reason so
            // the API still explains that the real issue was tolerance drift.
            if (amountMatches && statusMatches && timestampMatches) {
                matched.add(toRecord(internal, provider, timestampDifference, ComparisonReason.EXACT_MATCH));
            } else if (!amountMatches) {
                amountMismatch.add(toRecord(internal, provider, timestampDifference, ComparisonReason.AMOUNT_MISMATCH));
            } else if (!statusMatches) {
                statusMismatch.add(toRecord(internal, provider, timestampDifference, ComparisonReason.STATUS_MISMATCH));
            } else {
                statusMismatch.add(toRecord(internal, provider, timestampDifference, ComparisonReason.TIMESTAMP_OUTSIDE_TOLERANCE));
            }
        }

        for (NormalizedTransaction provider : providerByTxnId.values()) {
            missingInInternal.add(toRecord(null, provider, 0, ComparisonReason.MISSING_IN_INTERNAL));
        }

        // Sorting by txn_id keeps reruns deterministic and makes results easier to debug.
        Comparator<ReconciliationRecord> comparator = Comparator.comparing(ReconciliationRecord::txnId);
        matched.sort(comparator);
        amountMismatch.sort(comparator);
        statusMismatch.sort(comparator);
        missingInProvider.sort(comparator);
        missingInInternal.sort(comparator);

        return new ReconciliationOutcome(
                matched,
                amountMismatch,
                statusMismatch,
                missingInProvider,
                missingInInternal,
                new ReconciliationSummary(
                        jobId,
                        internalTransactions.size(),
                        providerTransactions.size(),
                        matched.size(),
                        amountMismatch.size(),
                        statusMismatch.size(),
                        missingInProvider.size(),
                        missingInInternal.size(),
                        toleranceSeconds
                )
        );
    }

    public ReconciliationCategory categoryForReason(ComparisonReason reason) {
        return switch (reason) {
            case EXACT_MATCH -> ReconciliationCategory.MATCHED;
            case AMOUNT_MISMATCH -> ReconciliationCategory.AMOUNT_MISMATCH;
            case STATUS_MISMATCH, TIMESTAMP_OUTSIDE_TOLERANCE -> ReconciliationCategory.STATUS_MISMATCH;
            case MISSING_IN_PROVIDER -> ReconciliationCategory.MISSING_IN_PROVIDER;
            case MISSING_IN_INTERNAL -> ReconciliationCategory.MISSING_IN_INTERNAL;
        };
    }

    private Map<String, NormalizedTransaction> indexByTxnId(List<NormalizedTransaction> transactions) {
        Map<String, NormalizedTransaction> index = new HashMap<>();
        for (NormalizedTransaction transaction : transactions) {
            index.put(transaction.txnId(), transaction);
        }
        return index;
    }

    private ReconciliationRecord toRecord(
            NormalizedTransaction internal,
            NormalizedTransaction provider,
            long timestampDifferenceSeconds,
            ComparisonReason reason
    ) {
        String txnId = internal != null ? internal.txnId() : provider.txnId();
        return new ReconciliationRecord(
                txnId,
                internal != null ? internal.amount() : null,
                provider != null ? provider.amount() : null,
                internal != null ? internal.status() : null,
                provider != null ? provider.status() : null,
                internal != null ? internal.timestamp() : null,
                provider != null ? provider.timestamp() : null,
                timestampDifferenceSeconds,
                reason
        );
    }
}
