package com.assignment.reconciliation.api;

import com.assignment.reconciliation.api.dto.ReconciliationResponse;
import com.assignment.reconciliation.api.dto.ReconciliationRunRequest;
import com.assignment.reconciliation.api.dto.ReconciliationSummaryResponse;
import com.assignment.reconciliation.service.ReconciliationRequest;
import com.assignment.reconciliation.service.ReconciliationService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Minimal REST surface for starting reconciliation jobs and reading stored results.
 */
@RestController
@RequestMapping("/api/reconciliations")
public class ReconciliationController {

    private final ReconciliationService reconciliationService;

    public ReconciliationController(ReconciliationService reconciliationService) {
        this.reconciliationService = reconciliationService;
    }

    @PostMapping("/run")
    @ResponseStatus(HttpStatus.CREATED)
    public ReconciliationResponse run(@RequestBody(required = false) ReconciliationRunRequest request) {
        ReconciliationRunRequest effectiveRequest = request == null
                ? new ReconciliationRunRequest(null, null, true)
                : request;
        // If neither file path is supplied, default to the bundled sample datasets so the
        // project is easy to demo locally and in the interview.
        boolean useSampleData = Boolean.TRUE.equals(effectiveRequest.useSampleData())
                || (isBlank(effectiveRequest.internalFilePath()) && isBlank(effectiveRequest.providerFilePath()));

        return reconciliationService.run(new ReconciliationRequest(
                effectiveRequest.internalFilePath(),
                effectiveRequest.providerFilePath(),
                useSampleData
        ));
    }

    @GetMapping("/{jobId}")
    public ReconciliationResponse getByJobId(@PathVariable String jobId) {
        return reconciliationService.getByJobId(jobId);
    }

    @GetMapping("/{jobId}/summary")
    public ReconciliationSummaryResponse getSummary(@PathVariable String jobId) {
        return reconciliationService.getSummary(jobId);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
