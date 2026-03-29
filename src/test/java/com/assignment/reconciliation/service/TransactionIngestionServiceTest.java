package com.assignment.reconciliation.service;

import com.assignment.reconciliation.api.dto.TransactionInputRecord;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies normalization rules and input validation edge cases.
 */
class TransactionIngestionServiceTest {

    private final TransactionIngestionService ingestionService = new TransactionIngestionService(new ObjectMapper());

    @Test
    void shouldNormalizeAndValidateRecords() {
        ValidationResult result = ingestionService.validateAndNormalize(List.of(
                new TransactionInputRecord(" T1 ", new BigDecimal("100.00"), " success ", "2026-03-20T10:00:00Z")
        ), "internal");

        assertThat(result.errors()).isEmpty();
        assertThat(result.transactions()).hasSize(1);
        assertThat(result.transactions().getFirst().txnId()).isEqualTo("T1");
        assertThat(result.transactions().getFirst().status()).isEqualTo("SUCCESS");
        assertThat(result.transactions().getFirst().amount().toPlainString()).isEqualTo("100");
    }

    @Test
    void shouldDetectDuplicateAndMalformedRows() {
        ValidationResult result = ingestionService.validateAndNormalize(List.of(
                new TransactionInputRecord("T1", new BigDecimal("100"), "SUCCESS", "2026-03-20T10:00:00Z"),
                new TransactionInputRecord("T1", new BigDecimal("100"), "SUCCESS", "bad-timestamp"),
                new TransactionInputRecord("T2", null, "SUCCESS", "2026-03-20T10:00:00Z")
        ), "provider");

        assertThat(result.errors()).hasSize(2);
        assertThat(result.errors()).anyMatch(error -> error.contains("duplicate txn_id 'T1'"));
        assertThat(result.errors()).anyMatch(error -> error.contains("missing amount"));
    }
}
