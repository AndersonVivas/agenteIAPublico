package com.pichincha.cdemsaspopenaidocumentation.copilot;

import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.CopilotExecutionOutput;
import java.nio.file.Path;

public interface PromptCommandExecutor {

  CopilotExecutionOutput execute(String prompt, Path workingDirectory);
}
