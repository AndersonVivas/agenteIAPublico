package com.pichincha.cdemsaspopenaidocumentation.copilot.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pichincha.cdemsaspopenaidocumentation.copilot.DocumentationContextRegistry;
import com.pichincha.cdemsaspopenaidocumentation.copilot.DocumentationGenerationService;
import com.pichincha.cdemsaspopenaidocumentation.copilot.ChangelogService;
import com.pichincha.cdemsaspopenaidocumentation.copilot.DocumentPersistenceService;
import com.pichincha.cdemsaspopenaidocumentation.copilot.DocumentStateRepository;
import com.pichincha.cdemsaspopenaidocumentation.copilot.PromptCommandExecutor;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.CopilotExecutionOutput;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentStateSnapshot;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentationGenerationContext;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.PromptExecutionResult;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.PromptExecutionStatus;
import com.pichincha.cdemsaspopenaidocumentation.domain.CodeScanSummary;
import com.pichincha.cdemsaspopenaidocumentation.domain.CodeSignalEvidence;
import com.pichincha.cdemsaspopenaidocumentation.domain.DocumentationGenerationRequest;
import com.pichincha.cdemsaspopenaidocumentation.domain.ScanExecutionMetadata;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * Validates that the knowledge-base pipeline wires end to end without making any real
 * external Copilot CLI calls: the {@link PromptCommandExecutor} boundary is stubbed with
 * deterministic JSON responses matching each agent category's contract.
 */
@SpringBootTest(properties = "documentation.pipeline-mode=KNOWLEDGE_BASE_PIPELINE")
class KnowledgePipelineIntegrationTest {

  private static final AtomicInteger EXECUTIONS = new AtomicInteger();

  @Autowired
  private DocumentationGenerationService documentationGenerationService;

  @Autowired
  private DocumentationContextRegistry contextRegistry;

  @Test
  void shouldExecuteKnowledgePipelineUsingOnlyStubbedCliCalls() {
    EXECUTIONS.set(0);
    DocumentationGenerationContext context = context();
    contextRegistry.register(context);

    PromptExecutionResult result = documentationGenerationService.generate(
      new DocumentationGenerationRequest(context.scanId(), "error-manual"));

    assertNotNull(result);
    assertEquals(PromptExecutionStatus.SUCCESS, result.executionStatus());
    assertTrue(EXECUTIONS.get() >= 1);
  }

  private DocumentationGenerationContext context() {
    CodeSignalEvidence evidence = new CodeSignalEvidence("api-1", "scan-kb", "API",
        "code", "java", "spring", "ApiController.java", "getPayments", "scope",
        "API_ROUTE", "API_ROUTE", "@GetMapping /payments", "high", "runtime",
        List.of("http"), "cause", "action", 10, 10, 10, List.of("line"), 0.9,
        List.of("tag"));
    ScanExecutionMetadata metadata = new ScanExecutionMetadata("fp", false, "", "",
        "FULL", "2.0", "2.2", 10L, 1L, 0L, 1L, 0L, 0L, 0L, 0L, 1L, 100L);
    CodeScanSummary summary = new CodeScanSummary("scan-kb", "fp", Instant.now(), 1,
        1, 0, 0, 1, Map.of("java", 1L), Map.of("java", 1L), List.of(), List.of(),
        List.of(), List.of(evidence), metadata);
    return new DocumentationGenerationContext("scan-kb", "sample-repo", isolatedRepositoryPath(),
        "BancoPichinchaEC", "taa-serviciosadministrativos", 42L, summary);
  }

  private String isolatedRepositoryPath() {
    try {
      return Files.createTempDirectory("kb-pipeline-test").toString();
    } catch (IOException exception) {
      throw new IllegalStateException("Failed to create isolated test repository path.",
          exception);
    }
  }

  @TestConfiguration
  static class TestConfig {

    @Bean
    @Primary
    PromptCommandExecutor promptCommandExecutor() {
      return new PromptCommandExecutor() {
        @Override
        public CopilotExecutionOutput execute(String prompt) {
          return execute(prompt, "");
        }

        @Override
        public CopilotExecutionOutput execute(String prompt, String model) {
          EXECUTIONS.incrementAndGet();
          return new CopilotExecutionOutput(stubResponseFor(prompt), "",
              PromptExecutionStatus.SUCCESS);
        }

        private String stubResponseFor(String prompt) {
          if (prompt.contains("Agente de Consolidación")) {
            return "{\"findings\":[],\"unresolvedConflicts\":[],\"warnings\":[]}";
          }
          if (prompt.contains("Agente de Renderizado de Plantilla")) {
            return "{}";
          }
          return "{\"findings\":[],\"gaps\":[],\"warnings\":[]}";
        }
      };
    }

    @Bean
    @Primary
    DocumentStateRepository documentStateRepository() {
      return new DocumentStateRepository() {
        @Override
        public Optional<DocumentStateSnapshot> load(String repositoryPath,
            String outputDirectory, String centralizedOutputPath, String documentName) {
          return Optional.empty();
        }

        @Override
        public void save(String repositoryPath, String outputDirectory,
            String centralizedOutputPath, String documentName, DocumentStateSnapshot snapshot) {
        }
      };
    }

    @Bean
    @Primary
    DocumentPersistenceService documentPersistenceService() {
      return (repositoryPath, outputDirectory, centralizedOutputPath, documentName, content,
          overwriteExisting) -> Path.of(repositoryPath, outputDirectory, documentName);
    }

    @Bean
    @Primary
    ChangelogService changelogService() {
      return (repositoryPath, outputDirectory, centralizedOutputPath, entry) -> { };
    }
  }
}
