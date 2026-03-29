package com.assignment.reconciliation.cli;

import com.assignment.reconciliation.api.dto.ReconciliationResponse;
import com.assignment.reconciliation.service.ReconciliationRequest;
import com.assignment.reconciliation.service.ReconciliationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Optional CLI entry point so the assignment can be demonstrated without Postman/curl.
 */
@Component
public class CliReconciliationRunner implements ApplicationRunner {

    private final ReconciliationService reconciliationService;
    private final ObjectMapper objectMapper;
    private final ConfigurableApplicationContext applicationContext;

    public CliReconciliationRunner(
            ReconciliationService reconciliationService,
            ObjectMapper objectMapper,
            ConfigurableApplicationContext applicationContext
    ) {
        this.reconciliationService = reconciliationService;
        this.objectMapper = objectMapper;
        this.applicationContext = applicationContext;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!args.containsOption("cli")) {
            return;
        }

        boolean useSampleData = args.containsOption("cli.useSampleData")
                || (!args.containsOption("cli.internal") && !args.containsOption("cli.provider"));
        String internalPath = getOption(args, "cli.internal");
        String providerPath = getOption(args, "cli.provider");

        ReconciliationResponse response = reconciliationService.run(new ReconciliationRequest(
                internalPath,
                providerPath,
                useSampleData
        ));

        String payload = toJson(response);
        System.out.println(payload);

        String outputPath = getOption(args, "cli.output");
        if (outputPath != null && !outputPath.isBlank()) {
            // Writing the response to disk makes it easy to attach sample output to the repo.
            writeOutput(outputPath, payload);
        }

        applicationContext.close();
    }

    private String getOption(ApplicationArguments args, String name) {
        return args.containsOption(name) ? args.getOptionValues(name).getFirst() : null;
    }

    private String toJson(ReconciliationResponse response) throws JsonProcessingException {
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response);
    }

    private void writeOutput(String outputPath, String payload) throws IOException {
        Path path = Path.of(outputPath);
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }
        Files.writeString(path, payload);
    }
}
