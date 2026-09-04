package com.pichincha.cdemsaspopenaidocumentation.copilot;

import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.PromptExecutionRequest;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.PromptExecutionResult;

public interface PromptExecutionService {

  PromptExecutionResult execute(PromptExecutionRequest request);
}
