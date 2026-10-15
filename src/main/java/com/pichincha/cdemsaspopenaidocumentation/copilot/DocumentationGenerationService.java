package com.pichincha.cdemsaspopenaidocumentation.copilot;

import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.PromptExecutionResult;
import com.pichincha.cdemsaspopenaidocumentation.domain.DocumentationGenerationRequest;

public interface DocumentationGenerationService {

  PromptExecutionResult generate(DocumentationGenerationRequest request);
}