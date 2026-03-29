package com.assignment.reconciliation.persistence.entity;

import com.assignment.reconciliation.domain.ComparisonReason;
import com.assignment.reconciliation.domain.ReconciliationCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Persisted row-level result for one transaction in one reconciliation category.
 */
@Entity
@Table(name = "reconciliation_results")
public class ReconciliationResultEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "job_id", nullable = false)
    private ReconciliationJobEntity job;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ReconciliationCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64)
    private ComparisonReason reason;

    @Column(nullable = false, length = 128)
    private String txnId;

    @Column(precision = 19, scale = 4)
    private BigDecimal internalAmount;

    @Column(precision = 19, scale = 4)
    private BigDecimal providerAmount;

    @Column(length = 64)
    private String internalStatus;

    @Column(length = 64)
    private String providerStatus;

    private Instant internalTimestamp;

    private Instant providerTimestamp;

    @Column(nullable = false)
    private long timestampDifferenceSeconds;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ReconciliationJobEntity getJob() {
        return job;
    }

    public void setJob(ReconciliationJobEntity job) {
        this.job = job;
    }

    public ReconciliationCategory getCategory() {
        return category;
    }

    public void setCategory(ReconciliationCategory category) {
        this.category = category;
    }

    public ComparisonReason getReason() {
        return reason;
    }

    public void setReason(ComparisonReason reason) {
        this.reason = reason;
    }

    public String getTxnId() {
        return txnId;
    }

    public void setTxnId(String txnId) {
        this.txnId = txnId;
    }

    public BigDecimal getInternalAmount() {
        return internalAmount;
    }

    public void setInternalAmount(BigDecimal internalAmount) {
        this.internalAmount = internalAmount;
    }

    public BigDecimal getProviderAmount() {
        return providerAmount;
    }

    public void setProviderAmount(BigDecimal providerAmount) {
        this.providerAmount = providerAmount;
    }

    public String getInternalStatus() {
        return internalStatus;
    }

    public void setInternalStatus(String internalStatus) {
        this.internalStatus = internalStatus;
    }

    public String getProviderStatus() {
        return providerStatus;
    }

    public void setProviderStatus(String providerStatus) {
        this.providerStatus = providerStatus;
    }

    public Instant getInternalTimestamp() {
        return internalTimestamp;
    }

    public void setInternalTimestamp(Instant internalTimestamp) {
        this.internalTimestamp = internalTimestamp;
    }

    public Instant getProviderTimestamp() {
        return providerTimestamp;
    }

    public void setProviderTimestamp(Instant providerTimestamp) {
        this.providerTimestamp = providerTimestamp;
    }

    public long getTimestampDifferenceSeconds() {
        return timestampDifferenceSeconds;
    }

    public void setTimestampDifferenceSeconds(long timestampDifferenceSeconds) {
        this.timestampDifferenceSeconds = timestampDifferenceSeconds;
    }
}
