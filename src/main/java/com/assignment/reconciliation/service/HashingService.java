package com.assignment.reconciliation.service;

import com.assignment.reconciliation.domain.NormalizedTransaction;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

@Service
public class HashingService {

    private final ObjectMapper canonicalObjectMapper;

    public HashingService(ObjectMapper objectMapper) {
        this.canonicalObjectMapper = JsonMapper.builder()
                .findAndAddModules()
                .enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
                .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
                .build();
    }

    public String hashTransactions(List<NormalizedTransaction> transactions) {
        return sha256(writeValue(transactions));
    }

    public String buildIdempotencyKey(String internalHash, String providerHash, long toleranceSeconds) {
        // The idempotency key changes only when the effective input changes:
        // internal data, provider data, or the configured tolerance.
        return sha256(writeValue(Map.of(
                "internalHash", internalHash,
                "providerHash", providerHash,
                "toleranceSeconds", toleranceSeconds
        )));
    }

    private String writeValue(Object value) {
        try {
            return canonicalObjectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to serialize reconciliation payload", exception);
        }
    }

    private String sha256(String payload) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(payload.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm not available", exception);
        }
    }
}
