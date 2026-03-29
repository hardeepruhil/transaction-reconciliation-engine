package com.assignment.reconciliation.service;

/**
 * Internal service-layer request used by both the REST controller and the CLI runner.
 */
public record ReconciliationRequest(
        String internalPath,
        String providerPath,
        boolean useSampleData
) {
}
