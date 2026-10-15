package com.pichincha.cdemsaspopenaidocumentation.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pichincha.cdemsaspopenaidocumentation.domain.RepositoryCloneRequest;
import com.pichincha.cdemsaspopenaidocumentation.domain.RepositoryCloneResponse;
import com.pichincha.cdemsaspopenaidocumentation.domain.RepositoryCloneResult;
import com.pichincha.cdemsaspopenaidocumentation.service.RepositoryProcessingOrchestrator;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class RepositoryAgentControllerTest {

  @Test
  void cloneRepositoryShouldDelegateToOrchestratorWithDefaultTemplate() {
    AtomicReference<Boolean> capturedFlag = new AtomicReference<>();
    AtomicReference<String> capturedTemplateId = new AtomicReference<>();
    RepositoryProcessingOrchestrator orchestrator = (request, generateDocumentation,
        templateId) -> {
      capturedFlag.set(generateDocumentation);
      capturedTemplateId.set(templateId);
      return RepositoryCloneResponse.direct(
          new RepositoryCloneResult(request.repositoryName(), request.pullRequestId(),
              "/tmp/sample-repo", "CLONED"), null);
    };
    RepositoryAgentController controller = new RepositoryAgentController(orchestrator);

    RepositoryCloneResponse response = controller.cloneRepository(
        new RepositoryCloneRequest("BancoPichinchaEC", "taa-serviciosadministrativos",
            "sample-repo", 42L), true).getBody();

    assertEquals("sample-repo", response.repositoryName());
    assertEquals(42L, response.pullRequestId());
    assertEquals("DIRECT", response.orchestrationMode());
    assertTrue(capturedFlag.get());
    assertEquals("error-manual", capturedTemplateId.get());
  }

  @Test
  void cloneRepositoryShouldForwardGenerateDocumentationFlagWhenDisabled() {
    AtomicReference<Boolean> capturedFlag = new AtomicReference<>();
    RepositoryProcessingOrchestrator orchestrator = (request, generateDocumentation,
        templateId) -> {
      capturedFlag.set(generateDocumentation);
      return RepositoryCloneResponse.direct(
          new RepositoryCloneResult(request.repositoryName(), request.pullRequestId(),
              "/tmp/sample-repo", "CLONED"), null);
    };
    RepositoryAgentController controller = new RepositoryAgentController(orchestrator);

    controller.cloneRepository(
        new RepositoryCloneRequest("BancoPichinchaEC", "taa-serviciosadministrativos",
            "sample-repo", 42L), false);

    assertFalse(capturedFlag.get());
  }
}
