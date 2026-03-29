package com.assignment.reconciliation.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Externalized settings for tolerance and bundled sample-data locations.
 */
@ConfigurationProperties(prefix = "app.reconciliation")
public record ReconciliationProperties(
        long timestampToleranceSeconds,
        String sampleInternalPath,
        String sampleProviderPath
) {
}
