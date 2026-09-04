package com.pichincha.cdemsaspopenaidocumentation.copilot.domain;

import org.apache.commons.lang3.StringUtils;

public record CopilotExecutionOutput(
    String stdout,
    String stderr,
    PromptExecutionStatus status) {

  public CopilotExecutionOutput {
    stdout = StringUtils.defaultString(stdout);
    stderr = StringUtils.defaultString(stderr);
  }
}
