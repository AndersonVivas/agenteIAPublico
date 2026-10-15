package com.pichincha.cdemsaspopenaidocumentation.copilot;

import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.CopilotExecutionOutput;

public interface PromptCommandExecutor {

  CopilotExecutionOutput execute(String prompt);

  default CopilotExecutionOutput execute(String prompt, String model) {
    return execute(prompt);
  }
}
