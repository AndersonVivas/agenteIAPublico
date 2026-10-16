package com.pichincha.cdemsaspopenaidocumentation.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pichincha.cdemsaspopenaidocumentation.domain.RepositoryCloneRequest;
import com.pichincha.cdemsaspopenaidocumentation.domain.RepositoryCloneResponse;
import com.pichincha.cdemsaspopenaidocumentation.service.RepositoryProcessingOrchestrator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class RepositoryAgentControllerTest {

  @Test
  void cloneRepositoryShouldDelegateRequestedTemplatesToOrchestrator() {
    AtomicReference<RepositoryCloneRequest> capturedRequest = new AtomicReference<>();
    AtomicReference<Boolean> capturedFlag = new AtomicReference<>();
    RepositoryProcessingOrchestrator orchestrator = (request, generateDocumentation) -> {
      capturedRequest.set(request);
      capturedFlag.set(generateDocumentation);
      return new RepositoryCloneResponse(request.repositoryName(), request.pullRequestId(),
          "workflow-1", null, List.of());
    };

    RepositoryCloneResponse response = new RepositoryAgentController(orchestrator)
        .cloneRepository(request(List.of("error-manual")), true).getBody();

    assertEquals("sample-repo", response.repositoryName());
    assertEquals(42L, response.pullRequestId());
    assertTrue(capturedFlag.get());
    assertEquals(List.of("error-manual"), capturedRequest.get().templateIds());
  }

  @Test
  void cloneRepositoryShouldForwardGenerateDocumentationFlagWhenDisabled() {
    AtomicReference<Boolean> capturedFlag = new AtomicReference<>();
    RepositoryProcessingOrchestrator orchestrator = (request, generateDocumentation) -> {
      capturedFlag.set(generateDocumentation);
      return new RepositoryCloneResponse(request.repositoryName(), request.pullRequestId(),
          "workflow-1", null, List.of());
    };

    new RepositoryAgentController(orchestrator).cloneRepository(request(null), false);

    assertFalse(capturedFlag.get());
  }

  private RepositoryCloneRequest request(List<String> templateIds) {
    return new RepositoryCloneRequest("BancoPichinchaEC", "taa-serviciosadministrativos",
        "sample-repo", 42L, templateIds, false);
  }
}
