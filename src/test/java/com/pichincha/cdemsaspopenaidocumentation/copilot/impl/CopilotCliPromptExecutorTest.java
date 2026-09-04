package com.pichincha.cdemsaspopenaidocumentation.copilot.impl;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pichincha.cdemsaspopenaidocumentation.copilot.config.CopilotCliProperties;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.jupiter.api.Test;

class CopilotCliPromptExecutorTest {

  @Test
  void shouldBuildPromptCommandWithoutModelArgument() throws Exception {
    CopilotCliProperties properties = new CopilotCliProperties("copilot", List.of("-p"),
      60);
    CopilotCliPromptExecutor executor = new CopilotCliPromptExecutor(properties);

    Method method = CopilotCliPromptExecutor.class.getDeclaredMethod("buildPromptCommand",
        String.class, String.class);
    method.setAccessible(true);

    @SuppressWarnings("unchecked")
    List<String> command = (List<String>) method.invoke(executor, "copilot",
        "Fix the bug in main.js");

    assertTrue(!command.contains("--model"));
    assertTrue(command.contains("-p"));
    assertTrue(!command.contains("--allow-all-tools"));
    assertTrue(command.get(command.size() - 1).equals("Fix the bug in main.js"));
  }
}
