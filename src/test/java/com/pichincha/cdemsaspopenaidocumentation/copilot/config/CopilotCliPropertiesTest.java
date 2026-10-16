package com.pichincha.cdemsaspopenaidocumentation.copilot.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CopilotCliPropertiesTest {

  @Test
  void shouldApplyDefaultsWhenValuesAreMissing() {
    CopilotCliProperties properties = new CopilotCliProperties(" ", 0);

    assertEquals("copilot", properties.command());
    assertEquals(300, properties.timeoutSeconds());
  }

  @Test
  void shouldKeepConfiguredValues() {
    CopilotCliProperties properties = new CopilotCliProperties("gh-copilot", 60);

    assertEquals("gh-copilot", properties.command());
    assertEquals(60, properties.timeoutSeconds());
  }
}
