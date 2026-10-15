package com.pichincha.cdemsaspopenaidocumentation.controller;

import com.pichincha.cdemsaspopenaidocumentation.copilot.DocumentationGenerationService;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.PromptExecutionResult;
import com.pichincha.cdemsaspopenaidocumentation.domain.DocumentationGenerationRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/v1/repository-agent")
public class PromptExecutionController {

  private final DocumentationGenerationService documentationGenerationService;

  public PromptExecutionController(
      DocumentationGenerationService documentationGenerationService) {
    this.documentationGenerationService = documentationGenerationService;
  }

  @PostMapping("/execute-prompt")
  public ResponseEntity<PromptExecutionResult> executePrompt(
      @Valid @RequestBody DocumentationGenerationRequest request) {
    return ResponseEntity.ok(documentationGenerationService.generate(request));
  }
}
