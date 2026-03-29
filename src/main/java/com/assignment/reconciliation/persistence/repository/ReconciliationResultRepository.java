package com.assignment.reconciliation.persistence.repository;

import com.assignment.reconciliation.persistence.entity.ReconciliationResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for individual reconciliation result rows.
 */
public interface ReconciliationResultRepository extends JpaRepository<ReconciliationResultEntity, Long> {
}
