package com.pichincha.cdemsaspopenaidocumentation.controller;

import com.pichincha.cdemsaspopenaidocumentation.domain.RepositoryCloneRequest;
import com.pichincha.cdemsaspopenaidocumentation.domain.RepositoryCloneResponse;
import com.pichincha.cdemsaspopenaidocumentation.service.RepositoryProcessingOrchestrator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/v1/repository-agent")
public class RepositoryAgentController {

  private final RepositoryProcessingOrchestrator orchestrator;

  @PostMapping("/clone")
  public ResponseEntity<RepositoryCloneResponse> cloneRepository(
      @Valid @RequestBody RepositoryCloneRequest request,
      @RequestParam(defaultValue = "true") boolean generateDocumentation) {
    return ResponseEntity.ok(orchestrator.process(request, generateDocumentation));
  }
}
