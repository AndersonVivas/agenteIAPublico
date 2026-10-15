package com.pichincha.cdemsaspopenaidocumentation.copilot;

import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.PromptExecutionRequest;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.PromptExecutionResult;

public interface PromptExecutionService {

  PromptExecutionResult execute(PromptExecutionRequest request);

  default PromptExecutionResult persistGeneratedDocument(PromptExecutionRequest request,
      String generatedContent) {
    throw new UnsupportedOperationException("Generated-document persistence is not supported.");
  }
}
