package com.assignment.reconciliation.cli;

import com.assignment.reconciliation.api.dto.ReconciliationResponse;
import com.assignment.reconciliation.api.dto.ReconciliationSummaryResponse;
import com.assignment.reconciliation.service.ReconciliationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.context.ConfigurableApplicationContext;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Confirms the optional CLI mode triggers reconciliation and can write output to disk.
 */
class CliReconciliationRunnerTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldSkipWhenCliFlagIsMissing() throws Exception {
        ReconciliationService service = mock(ReconciliationService.class);
        ConfigurableApplicationContext context = mock(ConfigurableApplicationContext.class);
        CliReconciliationRunner runner = new CliReconciliationRunner(service, new ObjectMapper(), context);

        runner.run(new DefaultApplicationArguments());

        verify(service, never()).run(any());
    }

    @Test
    void shouldWriteOutputFileWhenCliFlagIsPresent() throws Exception {
        ReconciliationService service = mock(ReconciliationService.class);
        ConfigurableApplicationContext context = mock(ConfigurableApplicationContext.class);
        when(service.run(any())).thenReturn(new ReconciliationResponse(
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                new ReconciliationSummaryResponse("job-1", 1, 1, 1, 0, 0, 0, 0, 5)
        ));

        Path output = tempDir.resolve("result.json");
        CliReconciliationRunner runner = new CliReconciliationRunner(service, new ObjectMapper(), context);

        runner.run(new DefaultApplicationArguments(
                "--cli",
                "--cli.useSampleData=true",
                "--cli.output=" + output
        ));

        assertThat(Files.exists(output)).isTrue();
        assertThat(Files.readString(output)).contains("\"job_id\"").contains("job-1");
    }
}
