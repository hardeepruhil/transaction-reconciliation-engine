package com.assignment.reconciliation.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * Raw input contract expected from JSON files before validation/normalization.
 */
public record TransactionInputRecord(
        @JsonProperty("txn_id")
        String txnId,
        BigDecimal amount,
        String status,
        String timestamp
) {
}
