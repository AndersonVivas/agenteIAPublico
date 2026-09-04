package com.pichincha.cdemsaspopenaidocumentation.copilot.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class CopilotCliPropertiesTest {

  @Test
  void shouldProvideDefaultPromptArguments() {
    CopilotCliProperties properties = new CopilotCliProperties("copilot", List.of(), 60);

    assertEquals(List.of("-p"), properties.arguments());
  }

  @Test
  void shouldIgnoreConfiguredArgumentsAndKeepPromptArgumentOnly() {
    CopilotCliProperties properties = new CopilotCliProperties("copilot",
        List.of("--temperature,0", "--allow-all-tools"), 60);

    assertEquals(List.of("-p"), properties.arguments());
  }
}
