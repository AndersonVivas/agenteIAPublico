package com.pichincha.cdemsaspopenaidocumentation.controller;

import com.pichincha.cdemsaspopenaidocumentation.copilot.PromptExecutionService;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.PromptExecutionResult;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.PromptExecutionStatus;
import com.pichincha.cdemsaspopenaidocumentation.domain.RepositoryCloneResponse;
import com.pichincha.cdemsaspopenaidocumentation.domain.RepositoryCloneResult;
import com.pichincha.cdemsaspopenaidocumentation.prompt.PromptPreviewService;
import com.pichincha.cdemsaspopenaidocumentation.prompt.domain.PromptType;
import com.pichincha.cdemsaspopenaidocumentation.service.RepositoryProcessingOrchestrator;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class RepositoryAgentControllerTest {

  private static final PromptExecutionService PROMPT_EXECUTION_SERVICE = request ->
      new PromptExecutionResult("exec-1", "generated", "/tmp/sample-repo/docs/sample-repo.md",
          "sample-repo.md", PromptExecutionStatus.SUCCESS, 12L,
          "github-copilot-cli", Instant.now());

  @Test
  void cloneRepositoryShouldReturnResponse() throws Exception {
    RepositoryProcessingOrchestrator orchestrator = request -> RepositoryCloneResponse.direct(
        new RepositoryCloneResult(request.repositoryName(), request.pullRequestId(),
        "/tmp/sample-repo", "CLONED"), null);
    PromptPreviewService promptPreviewService = new PromptPreviewService() {

      @Override
      public String buildPreview(
          com.pichincha.cdemsaspopenaidocumentation.domain.RepositoryCloneRequest request,
          RepositoryCloneResponse response, PromptType promptType) {
        return "preview";
      }

      @Override
      public String buildPreview(
          com.pichincha.cdemsaspopenaidocumentation.domain.RepositoryCloneRequest request,
          com.pichincha.cdemsaspopenaidocumentation.domain.CodeScanSummary codeScanSummary,
          String localPath, PromptType promptType) {
        return "preview";
      }
    };
    RepositoryAgentController controller = new RepositoryAgentController(orchestrator,
      promptPreviewService, PROMPT_EXECUTION_SERVICE);

    RepositoryCloneResponse response = controller.cloneRepository(
        new com.pichincha.cdemsaspopenaidocumentation.domain.RepositoryCloneRequest(
        "BancoPichinchaEC", "taa-serviciosadministrativos", "sample-repo",
      42L), false, false, null).getBody();

    org.junit.jupiter.api.Assertions.assertEquals("sample-repo", response.repositoryName());
    org.junit.jupiter.api.Assertions.assertEquals(42L, response.pullRequestId());
    org.junit.jupiter.api.Assertions.assertEquals("DIRECT", response.orchestrationMode());
    org.junit.jupiter.api.Assertions.assertEquals(List.of("CLONE", "SCAN_CODE"),
        response.stages());
    org.junit.jupiter.api.Assertions.assertEquals(null, response.promptPreview());
    org.junit.jupiter.api.Assertions.assertEquals(null, response.promptExecutionResult());
  }

  @Test
  void cloneRepositoryShouldReturnPromptPreviewWhenRequested() throws Exception {
    RepositoryProcessingOrchestrator orchestrator = request -> RepositoryCloneResponse.direct(
        new RepositoryCloneResult(request.repositoryName(), request.pullRequestId(),
        "/tmp/sample-repo", "CLONED"), null);
    PromptPreviewService promptPreviewService = new PromptPreviewService() {

      @Override
      public String buildPreview(
          com.pichincha.cdemsaspopenaidocumentation.domain.RepositoryCloneRequest request,
          RepositoryCloneResponse response, PromptType promptType) {
        return "prompt-preview";
      }

      @Override
      public String buildPreview(
          com.pichincha.cdemsaspopenaidocumentation.domain.RepositoryCloneRequest request,
          com.pichincha.cdemsaspopenaidocumentation.domain.CodeScanSummary codeScanSummary,
          String localPath, PromptType promptType) {
        return "prompt-preview";
      }
    };
    RepositoryAgentController controller = new RepositoryAgentController(orchestrator,
      promptPreviewService, PROMPT_EXECUTION_SERVICE);

    RepositoryCloneResponse response = controller.cloneRepository(
        new com.pichincha.cdemsaspopenaidocumentation.domain.RepositoryCloneRequest(
            "BancoPichinchaEC", "taa-serviciosadministrativos", "sample-repo", 42L),
        true, false, PromptType.README_GENERATION).getBody();

    org.junit.jupiter.api.Assertions.assertEquals("prompt-preview", response.promptPreview());
  }

  @Test
  void cloneRepositoryShouldGenerateDocumentationByDefault() throws Exception {
    RepositoryProcessingOrchestrator orchestrator = request -> RepositoryCloneResponse.direct(
        new RepositoryCloneResult(request.repositoryName(), request.pullRequestId(),
            "/tmp/sample-repo", "CLONED"), null);
    PromptPreviewService promptPreviewService = new PromptPreviewService() {

      @Override
      public String buildPreview(
          com.pichincha.cdemsaspopenaidocumentation.domain.RepositoryCloneRequest request,
          RepositoryCloneResponse response, PromptType promptType) {
        return "prompt";
      }

      @Override
      public String buildPreview(
          com.pichincha.cdemsaspopenaidocumentation.domain.RepositoryCloneRequest request,
          com.pichincha.cdemsaspopenaidocumentation.domain.CodeScanSummary codeScanSummary,
          String localPath, PromptType promptType) {
        return "prompt";
      }
    };
    RepositoryAgentController controller = new RepositoryAgentController(orchestrator,
        promptPreviewService, PROMPT_EXECUTION_SERVICE);

    RepositoryCloneResponse response = controller.cloneRepository(
        new com.pichincha.cdemsaspopenaidocumentation.domain.RepositoryCloneRequest(
            "BancoPichinchaEC", "taa-serviciosadministrativos", "sample-repo", 42L),
        false, true, PromptType.ERROR_MANUAL).getBody();

    org.junit.jupiter.api.Assertions.assertEquals(PromptExecutionStatus.SUCCESS,
        response.promptExecutionResult().executionStatus());
    org.junit.jupiter.api.Assertions.assertEquals("sample-repo.md",
        response.promptExecutionResult().documentName());
  }
}