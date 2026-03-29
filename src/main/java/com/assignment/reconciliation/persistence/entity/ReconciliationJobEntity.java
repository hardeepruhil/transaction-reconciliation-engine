package com.assignment.reconciliation.persistence.entity;

import com.assignment.reconciliation.domain.RunStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Persisted header row for a reconciliation run, storing metadata and aggregate counts.
 */
@Entity
@Table(name = "reconciliation_jobs")
public class ReconciliationJobEntity {

    @Id
    private String id;

    @Column(nullable = false, unique = true, length = 128)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private RunStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private long timestampToleranceSeconds;

    @Column(nullable = false, length = 128)
    private String internalHash;

    @Column(nullable = false, length = 128)
    private String providerHash;

    @Column(nullable = false)
    private int totalInternal;

    @Column(nullable = false)
    private int totalProvider;

    @Column(nullable = false)
    private int matchedCount;

    @Column(nullable = false)
    private int amountMismatchCount;

    @Column(nullable = false)
    private int statusMismatchCount;

    @Column(nullable = false)
    private int missingInProviderCount;

    @Column(nullable = false)
    private int missingInInternalCount;

    @OneToMany(mappedBy = "job")
    private List<ReconciliationResultEntity> results = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public RunStatus getStatus() {
        return status;
    }

    public void setStatus(RunStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public long getTimestampToleranceSeconds() {
        return timestampToleranceSeconds;
    }

    public void setTimestampToleranceSeconds(long timestampToleranceSeconds) {
        this.timestampToleranceSeconds = timestampToleranceSeconds;
    }

    public String getInternalHash() {
        return internalHash;
    }

    public void setInternalHash(String internalHash) {
        this.internalHash = internalHash;
    }

    public String getProviderHash() {
        return providerHash;
    }

    public void setProviderHash(String providerHash) {
        this.providerHash = providerHash;
    }

    public int getTotalInternal() {
        return totalInternal;
    }

    public void setTotalInternal(int totalInternal) {
        this.totalInternal = totalInternal;
    }

    public int getTotalProvider() {
        return totalProvider;
    }

    public void setTotalProvider(int totalProvider) {
        this.totalProvider = totalProvider;
    }

    public int getMatchedCount() {
        return matchedCount;
    }

    public void setMatchedCount(int matchedCount) {
        this.matchedCount = matchedCount;
    }

    public int getAmountMismatchCount() {
        return amountMismatchCount;
    }

    public void setAmountMismatchCount(int amountMismatchCount) {
        this.amountMismatchCount = amountMismatchCount;
    }

    public int getStatusMismatchCount() {
        return statusMismatchCount;
    }

    public void setStatusMismatchCount(int statusMismatchCount) {
        this.statusMismatchCount = statusMismatchCount;
    }

    public int getMissingInProviderCount() {
        return missingInProviderCount;
    }

    public void setMissingInProviderCount(int missingInProviderCount) {
        this.missingInProviderCount = missingInProviderCount;
    }

    public int getMissingInInternalCount() {
        return missingInInternalCount;
    }

    public void setMissingInInternalCount(int missingInInternalCount) {
        this.missingInInternalCount = missingInInternalCount;
    }

    public List<ReconciliationResultEntity> getResults() {
        return results;
    }

    public void setResults(List<ReconciliationResultEntity> results) {
        this.results = results;
    }
}
