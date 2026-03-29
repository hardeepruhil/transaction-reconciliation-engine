package com.assignment.reconciliation.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Enables strongly typed application configuration for reconciliation settings.
 */
@Configuration
@EnableConfigurationProperties(ReconciliationProperties.class)
public class AppConfig {
}
