package com.assignment.reconciliation.persistence.repository;

import com.assignment.reconciliation.persistence.entity.ReconciliationJobEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Repository for job headers, including fetch plans that eagerly load result rows when needed.
 */
public interface ReconciliationJobRepository extends JpaRepository<ReconciliationJobEntity, String> {

    Optional<ReconciliationJobEntity> findByIdempotencyKey(String idempotencyKey);

    @EntityGraph(attributePaths = "results")
    @Query("select j from ReconciliationJobEntity j where j.id = :id")
    Optional<ReconciliationJobEntity> findDetailedById(@Param("id") String id);

    @EntityGraph(attributePaths = "results")
    @Query("select j from ReconciliationJobEntity j where j.idempotencyKey = :idempotencyKey")
    Optional<ReconciliationJobEntity> findDetailedByIdempotencyKey(@Param("idempotencyKey") String idempotencyKey);
}
