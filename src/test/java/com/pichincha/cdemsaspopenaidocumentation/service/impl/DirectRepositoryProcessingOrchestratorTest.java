package com.pichincha.cdemsaspopenaidocumentation.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.pichincha.cdemsaspopenaidocumentation.config.GitCloneProperties;
import com.pichincha.cdemsaspopenaidocumentation.copilot.DocumentationContextRegistry;
import com.pichincha.cdemsaspopenaidocumentation.copilot.DocumentationGenerationService;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentationGenerationContext;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.PromptExecutionResult;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.PromptExecutionStatus;
import com.pichincha.cdemsaspopenaidocumentation.domain.CodeScanSummary;
import com.pichincha.cdemsaspopenaidocumentation.domain.CommandExecutionResult;
import com.pichincha.cdemsaspopenaidocumentation.domain.DocumentationGenerationRequest;
import com.pichincha.cdemsaspopenaidocumentation.domain.RepositoryCloneRequest;
import com.pichincha.cdemsaspopenaidocumentation.domain.RepositoryCloneResponse;
import com.pichincha.cdemsaspopenaidocumentation.helper.GitCommandExecutor;
import com.pichincha.cdemsaspopenaidocumentation.helper.GitRepositoryCloner;
import com.pichincha.cdemsaspopenaidocumentation.service.CodeScanService;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DirectRepositoryProcessingOrchestratorTest {

  @TempDir
  Path cloneDirectory;

  private static final DocumentationContextRegistry EMPTY_REGISTRY =
      new DocumentationContextRegistry() {
        @Override
        public void register(DocumentationGenerationContext context) {
        }

        @Override
        public Optional<DocumentationGenerationContext> findByScanId(String scanId) {
          return Optional.empty();
        }
      };

  @Test
  void processShouldGenerateDocumentationWhenRequested() {
    AtomicReference<DocumentationGenerationRequest> capturedRequest = new AtomicReference<>();
    DocumentationGenerationService generationService = request -> {
      capturedRequest.set(request);
      return new PromptExecutionResult("exec-1", "generated",
          "/tmp/sample-repo/docs/sample-repo.md", "sample-repo.md",
          PromptExecutionStatus.SUCCESS, 12L, "github-copilot-cli", Instant.now());
    };
    DirectRepositoryProcessingOrchestrator orchestrator = new DirectRepositoryProcessingOrchestrator(
        gitRepositoryCloner(), path -> scanSummary(), EMPTY_REGISTRY, generationService);

    RepositoryCloneResponse response = orchestrator.process(cloneRequest(), true, "error-manual");

    assertEquals(PromptExecutionStatus.SUCCESS,
        response.promptExecutionResult().executionStatus());
    assertEquals("scan-1", capturedRequest.get().scanId());
    assertEquals("error-manual", capturedRequest.get().templateId());
  }

  @Test
  void processShouldSkipDocumentationWhenNotRequested() {
    CodeScanService scanService = path -> scanSummary();
    DirectRepositoryProcessingOrchestrator orchestrator = new DirectRepositoryProcessingOrchestrator(
        gitRepositoryCloner(), scanService, EMPTY_REGISTRY,
        request -> {
          throw new IllegalStateException("should not be invoked");
        });

    RepositoryCloneResponse response = orchestrator.process(cloneRequest(), false, "error-manual");

    assertNull(response.promptExecutionResult());
    assertEquals(List.of("CLONE", "SCAN_CODE"), response.stages());
  }

  private GitRepositoryCloner gitRepositoryCloner() {
    GitCloneProperties properties = new GitCloneProperties("https://dev.azure.com", "user", "pat",
        cloneDirectory, true, Duration.ofMinutes(5));
    GitCommandExecutor noOpExecutor = (command, workingDirectory, timeout) ->
        new CommandExecutionResult(0, "");
    return new GitRepositoryCloner(properties, noOpExecutor);
  }

  private RepositoryCloneRequest cloneRequest() {
    return new RepositoryCloneRequest("BancoPichinchaEC", "taa-serviciosadministrativos",
        "sample-repo", 42L);
  }

  private CodeScanSummary scanSummary() {
    return new CodeScanSummary("scan-1", "fp-1", Instant.now(), 1, 1, 0, 0,
        1, Map.of("java", 1L), Map.of("java", 1L), List.of(), List.of(),
        List.of(), List.of(), null);
  }
}
