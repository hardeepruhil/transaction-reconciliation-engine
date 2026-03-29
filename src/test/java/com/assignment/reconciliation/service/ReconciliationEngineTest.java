package com.assignment.reconciliation.service;

import com.assignment.reconciliation.domain.ComparisonReason;
import com.assignment.reconciliation.domain.NormalizedTransaction;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Focused unit tests for category assignment inside the core reconciliation algorithm.
 */
class ReconciliationEngineTest {

    private final ReconciliationEngine engine = new ReconciliationEngine();

    @Test
    void shouldCategorizeMatchesAndMismatchesDeterministically() {
        List<NormalizedTransaction> internal = List.of(
                transaction("T1", "100.00", "SUCCESS", "2026-03-20T10:00:00Z"),
                transaction("T2", "500.00", "SUCCESS", "2026-03-20T10:05:00Z"),
                transaction("T3", "200.00", "PENDING", "2026-03-20T10:08:00Z"),
                transaction("T4", "250.00", "SUCCESS", "2026-03-20T10:09:00Z")
        );
        List<NormalizedTransaction> provider = List.of(
                transaction("T1", "100", "SUCCESS", "2026-03-20T10:00:05Z"),
                transaction("T2", "400", "SUCCESS", "2026-03-20T10:05:02Z"),
                transaction("T3", "200", "FAILED", "2026-03-20T10:08:00Z"),
                transaction("T5", "25", "SUCCESS", "2026-03-20T10:10:00Z")
        );

        var result = engine.reconcile("job-1", internal, provider, 5);

        assertThat(result.matched()).extracting(record -> record.txnId()).containsExactly("T1");
        assertThat(result.amountMismatch()).extracting(record -> record.txnId()).containsExactly("T2");
        assertThat(result.statusMismatch()).extracting(record -> record.txnId()).containsExactly("T3");
        assertThat(result.missingInProvider()).extracting(record -> record.txnId()).containsExactly("T4");
        assertThat(result.missingInInternal()).extracting(record -> record.txnId()).containsExactly("T5");
    }

    @Test
    void shouldTreatTimestampOutsideToleranceAsStatusMismatchBucketWithReason() {
        var internal = List.of(transaction("T9", "10", "SUCCESS", "2026-03-20T10:00:00Z"));
        var provider = List.of(transaction("T9", "10", "SUCCESS", "2026-03-20T10:00:30Z"));

        var result = engine.reconcile("job-2", internal, provider, 5);

        assertThat(result.statusMismatch()).hasSize(1);
        assertThat(result.statusMismatch().getFirst().reason()).isEqualTo(ComparisonReason.TIMESTAMP_OUTSIDE_TOLERANCE);
    }

    private NormalizedTransaction transaction(String txnId, String amount, String status, String timestamp) {
        return new NormalizedTransaction(
                txnId,
                new BigDecimal(amount).stripTrailingZeros(),
                status,
                Instant.parse(timestamp)
        );
    }
}
