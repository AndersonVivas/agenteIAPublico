package com.pichincha.cdemsaspopenaidocumentation.copilot.config;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "github.copilot")
public record CopilotCliProperties(
    String command,
    List<String> arguments,
    long timeoutSeconds) {

  private static final String PROMPT_ARGUMENT = "-p";

  public CopilotCliProperties {
    command = StringUtils.defaultIfBlank(command, "copilot");
    arguments = normalizeArguments(arguments);
    timeoutSeconds = timeoutSeconds <= 0 ? 300 : timeoutSeconds;
  }

  private static List<String> normalizeArguments(List<String> source) {
    return List.of(PROMPT_ARGUMENT);
  }
}
