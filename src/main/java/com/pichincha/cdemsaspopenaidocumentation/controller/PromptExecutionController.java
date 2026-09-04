package com.pichincha.cdemsaspopenaidocumentation.controller;

import com.pichincha.cdemsaspopenaidocumentation.copilot.PromptExecutionService;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.PromptExecutionRequest;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.PromptExecutionResult;
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

  private final PromptExecutionService promptExecutionService;

  public PromptExecutionController(PromptExecutionService promptExecutionService) {
    this.promptExecutionService = promptExecutionService;
  }

  @PostMapping("/execute-prompt")
  public ResponseEntity<PromptExecutionResult> executePrompt(
      @Valid @RequestBody PromptExecutionRequest request) {
    return ResponseEntity.ok(promptExecutionService.execute(request));
  }
}
