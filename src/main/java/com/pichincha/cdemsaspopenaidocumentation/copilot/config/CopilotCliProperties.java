package com.pichincha.cdemsaspopenaidocumentation.copilot.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "github.copilot")
public record CopilotCliProperties(String command, long timeoutSeconds) {

  private static final long DEFAULT_TIMEOUT_SECONDS = 300;

  public CopilotCliProperties {
    command = StringUtils.defaultIfBlank(command, "copilot");
    timeoutSeconds = timeoutSeconds <= 0 ? DEFAULT_TIMEOUT_SECONDS : timeoutSeconds;
  }
}
