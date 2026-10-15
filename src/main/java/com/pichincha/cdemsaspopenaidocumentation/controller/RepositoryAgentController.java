package com.pichincha.cdemsaspopenaidocumentation.controller;

import com.pichincha.cdemsaspopenaidocumentation.domain.RepositoryCloneRequest;
import com.pichincha.cdemsaspopenaidocumentation.domain.RepositoryCloneResponse;
import com.pichincha.cdemsaspopenaidocumentation.service.RepositoryProcessingOrchestrator;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/v1/repository-agent")
public class RepositoryAgentController {

  private static final String ERROR_MANUAL_TEMPLATE_ID = "error-manual";

  private final RepositoryProcessingOrchestrator orchestrator;

  public RepositoryAgentController(RepositoryProcessingOrchestrator orchestrator) {
    this.orchestrator = orchestrator;
  }

  @PostMapping("/clone")
  public ResponseEntity<RepositoryCloneResponse> cloneRepository(
      @Valid @RequestBody RepositoryCloneRequest request,
      @RequestParam(defaultValue = "true") boolean generateDocumentation) {
    RepositoryCloneResponse response = orchestrator.process(request, generateDocumentation,
        ERROR_MANUAL_TEMPLATE_ID);
    return ResponseEntity.ok(response);
  }

}