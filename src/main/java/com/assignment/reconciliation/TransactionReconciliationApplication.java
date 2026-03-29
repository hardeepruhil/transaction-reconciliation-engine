package com.assignment.reconciliation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot entry point for the reconciliation service.
 */
@SpringBootApplication
public class TransactionReconciliationApplication {

    public static void main(String[] args) {
        SpringApplication.run(TransactionReconciliationApplication.class, args);
    }
}
