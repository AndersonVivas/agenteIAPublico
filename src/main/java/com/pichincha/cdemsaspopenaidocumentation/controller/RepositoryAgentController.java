package com.pichincha.cdemsaspopenaidocumentation.controller;

import com.pichincha.cdemsaspopenaidocumentation.copilot.PromptExecutionService;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentTemplateType;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.PromptExecutionRequest;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.PromptExecutionResult;
import com.pichincha.cdemsaspopenaidocumentation.domain.RepositoryCloneRequest;
import com.pichincha.cdemsaspopenaidocumentation.domain.RepositoryCloneResponse;
import com.pichincha.cdemsaspopenaidocumentation.prompt.PromptPreviewService;
import com.pichincha.cdemsaspopenaidocumentation.prompt.domain.PromptType;
import com.pichincha.cdemsaspopenaidocumentation.service.RepositoryProcessingOrchestrator;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/v1/repository-agent")
public class RepositoryAgentController {

  private final RepositoryProcessingOrchestrator orchestrator;
  private final PromptPreviewService promptPreviewService;
  private final PromptExecutionService promptExecutionService;

  public RepositoryAgentController(RepositoryProcessingOrchestrator orchestrator,
      PromptPreviewService promptPreviewService,
      PromptExecutionService promptExecutionService) {
    this.orchestrator = orchestrator;
    this.promptPreviewService = promptPreviewService;
    this.promptExecutionService = promptExecutionService;
  }

  @PostMapping("/clone")
  public ResponseEntity<RepositoryCloneResponse> cloneRepository(
      @Valid @RequestBody RepositoryCloneRequest request,
      @RequestParam(defaultValue = "false") boolean includePrompt,
      @RequestParam(defaultValue = "true") boolean generateDocumentation,
      @RequestParam(required = false) PromptType promptType) {
    RepositoryCloneResponse response = orchestrator.process(request);
    if (!generateDocumentation && !includePrompt) {
      return ResponseEntity.ok(response);
    }
    String prompt = promptPreviewService.buildPreview(request, response, promptType);
    RepositoryCloneResponse enrichedResponse = includePrompt
        ? response.withPromptPreview(prompt)
        : response;
    if (!generateDocumentation) {
      return ResponseEntity.ok(enrichedResponse);
    }
    PromptExecutionResult executionResult = promptExecutionService.execute(
        toExecutionRequest(request, response, prompt, promptType));
    return ResponseEntity.ok(enrichedResponse.withPromptExecutionResult(executionResult));
  }

  private PromptExecutionRequest toExecutionRequest(RepositoryCloneRequest request,
      RepositoryCloneResponse response, String prompt, PromptType promptType) {
    return new PromptExecutionRequest(UUID.randomUUID().toString(),
        request.repositoryName(), response.localPath(), mapDocumentTemplateType(promptType),
        prompt, Map.of("organization", request.organization(),
            "projectName", request.projectName(),
            "pullRequestId", String.valueOf(request.pullRequestId())));
  }

  private DocumentTemplateType mapDocumentTemplateType(PromptType promptType) {
    if (promptType == null || promptType == PromptType.ERROR_MANUAL) {
      return DocumentTemplateType.ERROR_MANUAL;
    }
    if (promptType == PromptType.API_DOCUMENTATION) {
      return DocumentTemplateType.API_DOCUMENTATION;
    }
    if (promptType == PromptType.ARCHITECTURE_DOCUMENTATION) {
      return DocumentTemplateType.ARCHITECTURE_DOCUMENTATION;
    }
    return DocumentTemplateType.README;
  }
}