package com.assignment.reconciliation.api;

import com.assignment.reconciliation.persistence.repository.ReconciliationJobRepository;
import com.assignment.reconciliation.persistence.repository.ReconciliationResultRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end tests covering the REST API, persistence, and idempotent reruns.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ReconciliationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReconciliationJobRepository jobRepository;

    @Autowired
    private ReconciliationResultRepository resultRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        resultRepository.deleteAll();
        jobRepository.deleteAll();
    }

    @Test
    void shouldRunSampleDataAndReuseExistingJobOnSecondRun() throws Exception {
        String requestBody = """
                {
                  "use_sample_data": true
                }
                """;

        String firstResponse = mockMvc.perform(post("/api/reconciliations/run")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.matched[0].txn_id").value("T1"))
                .andExpect(jsonPath("$.amount_mismatch[0].txn_id").value("T2"))
                .andExpect(jsonPath("$.status_mismatch[0].txn_id").value("T3"))
                .andExpect(jsonPath("$.missing_in_provider[0].txn_id").value("T4"))
                .andExpect(jsonPath("$.missing_in_internal[0].txn_id").value("T5"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String secondResponse = mockMvc.perform(post("/api/reconciliations/run")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode firstJson = objectMapper.readTree(firstResponse);
        JsonNode secondJson = objectMapper.readTree(secondResponse);

        assertThat(jobRepository.count()).isEqualTo(1);
        assertThat(firstJson.path("summary").path("job_id").asText())
                .isEqualTo(secondJson.path("summary").path("job_id").asText());
        assertThat(firstJson.path("matched").get(0).path("txn_id").asText()).isEqualTo("T1");
        assertThat(secondJson.path("matched").get(0).path("txn_id").asText()).isEqualTo("T1");
        assertThat(firstJson.path("amount_mismatch").get(0).path("txn_id").asText()).isEqualTo("T2");
        assertThat(secondJson.path("amount_mismatch").get(0).path("txn_id").asText()).isEqualTo("T2");
        assertThat(firstJson.path("status_mismatch").get(0).path("txn_id").asText()).isEqualTo("T3");
        assertThat(secondJson.path("status_mismatch").get(0).path("txn_id").asText()).isEqualTo("T3");
        assertThat(firstJson.path("missing_in_provider").get(0).path("txn_id").asText()).isEqualTo("T4");
        assertThat(secondJson.path("missing_in_provider").get(0).path("txn_id").asText()).isEqualTo("T4");
        assertThat(firstJson.path("missing_in_internal").get(0).path("txn_id").asText()).isEqualTo("T5");
        assertThat(secondJson.path("missing_in_internal").get(0).path("txn_id").asText()).isEqualTo("T5");
    }

    @Test
    void shouldReturnJobSummary() throws Exception {
        String jobId = mockMvc.perform(post("/api/reconciliations/run")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content("{\"use_sample_data\":true}"))
                .andReturn()
                .getResponse()
                .getContentAsString()
                .replaceAll("(?s).*\"job_id\":\"([^\"]+)\".*", "$1");

        mockMvc.perform(get("/api/reconciliations/{jobId}/summary", jobId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.job_id").value(jobId))
                .andExpect(jsonPath("$.matched_count").value(1));
    }
}
