package com.assignment.reconciliation.service;

import com.assignment.reconciliation.api.dto.TransactionInputRecord;
import com.assignment.reconciliation.domain.NormalizedTransaction;
import com.assignment.reconciliation.exception.ValidationException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

@Service
public class TransactionIngestionService {

    private final ObjectMapper objectMapper;

    public TransactionIngestionService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<NormalizedTransaction> loadTransactions(String location, String datasetName) {
        try (InputStream inputStream = openStream(location)) {
            List<TransactionInputRecord> rawRecords = objectMapper.readValue(
                    inputStream,
                    new TypeReference<>() {
                    }
            );
            ValidationResult validationResult = validateAndNormalize(rawRecords, datasetName);
            if (!validationResult.errors().isEmpty()) {
                throw new ValidationException("Input validation failed", validationResult.errors());
            }
            return validationResult.transactions();
        } catch (IOException exception) {
            throw new ValidationException(
                    "Unable to read input files",
                    List.of("Could not read %s dataset from '%s': %s".formatted(datasetName, location, exception.getMessage()))
            );
        }
    }

    private InputStream openStream(String location) throws IOException {
        if (location == null || location.isBlank()) {
            throw new IOException("Path is empty");
        }
        String resolvedLocation = Objects.requireNonNull(location, "Path is empty");
        if (resolvedLocation.startsWith("classpath:")) {
            String classpathLocation = Objects.requireNonNull(
                    resolvedLocation.substring("classpath:".length()),
                    "Classpath resource path must not be null"
            );
            Resource resource = new ClassPathResource(classpathLocation);
            InputStream inputStream = resource.getInputStream();
            return Objects.requireNonNull(inputStream, "Classpath resource stream must not be null");
        }
        return Files.newInputStream(Path.of(resolvedLocation));
    }

    public ValidationResult validateAndNormalize(List<TransactionInputRecord> rawRecords, String datasetName) {
        List<String> errors = new ArrayList<>();
        List<NormalizedTransaction> normalizedTransactions = new ArrayList<>();
        Set<String> seenTxnIds = new HashSet<>();

        if (rawRecords == null || rawRecords.isEmpty()) {
            errors.add(datasetName + " dataset is empty");
            return new ValidationResult(List.of(), errors);
        }

        for (int index = 0; index < rawRecords.size(); index++) {
            TransactionInputRecord record = rawRecords.get(index);
            String prefix = "%s dataset record %d".formatted(datasetName, index);

            if (record == null) {
                errors.add(prefix + " is null");
                continue;
            }

            if (record.txnId() == null || record.txnId().isBlank()) {
                errors.add(prefix + " has missing txn_id");
                continue;
            }

            String normalizedTxnId = record.txnId().trim();
            if (!seenTxnIds.add(normalizedTxnId)) {
                // Duplicate transaction IDs are treated as invalid input so we do not silently
                // pick one record and hide a data-quality issue from the reconciliation result.
                errors.add("%s contains duplicate txn_id '%s'".formatted(datasetName, normalizedTxnId));
                continue;
            }

            if (record.amount() == null) {
                errors.add(prefix + " has missing amount");
                continue;
            }

            if (record.status() == null || record.status().isBlank()) {
                errors.add(prefix + " has missing status");
                continue;
            }

            if (record.timestamp() == null || record.timestamp().isBlank()) {
                errors.add(prefix + " has missing timestamp");
                continue;
            }

            try {
                normalizedTransactions.add(new NormalizedTransaction(
                        normalizedTxnId,
                        // BigDecimal keeps money comparisons precise and avoids floating-point drift.
                        normalizeAmount(record.amount()),
                        // Statuses are normalized so SUCCESS, success, and " success " compare equally.
                        record.status().trim().toUpperCase(Locale.ROOT),
                        Instant.parse(record.timestamp().trim())
                ));
            } catch (DateTimeParseException exception) {
                errors.add(prefix + " has malformed timestamp '" + record.timestamp() + "'");
            }
        }

        normalizedTransactions.sort(Comparator.comparing(NormalizedTransaction::txnId));
        return new ValidationResult(normalizedTransactions, errors);
    }

    private BigDecimal normalizeAmount(BigDecimal amount) {
        return amount.stripTrailingZeros();
    }
}
