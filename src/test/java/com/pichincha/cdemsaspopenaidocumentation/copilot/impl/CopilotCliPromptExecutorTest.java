package com.pichincha.cdemsaspopenaidocumentation.copilot.impl;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pichincha.cdemsaspopenaidocumentation.copilot.config.CopilotCliProperties;
import java.lang.reflect.Method;
import java.util.List;
import org.junit.jupiter.api.Test;

class CopilotCliPromptExecutorTest {

  @Test
  void shouldBuildPromptCommandWithoutModelArgument() throws Exception {
    CopilotCliProperties properties = new CopilotCliProperties("copilot", 60);
    CopilotCliPromptExecutor executor = new CopilotCliPromptExecutor(properties);

    Method method = CopilotCliPromptExecutor.class.getDeclaredMethod("buildPromptCommand",
        String.class, String.class, String.class);
    method.setAccessible(true);

    @SuppressWarnings("unchecked")
    List<String> command = (List<String>) method.invoke(executor, "copilot",
        "Fix the bug in main.js", "");

    assertTrue(!command.contains("--model"));
    assertTrue(command.contains("-p"));
    assertTrue(!command.contains("--allow-all-tools"));
    assertTrue(command.get(command.size() - 1).equals("Fix the bug in main.js"));
  }

  @Test
  void shouldAppendModelArgumentWhenModelIsProvided() throws Exception {
    CopilotCliProperties properties = new CopilotCliProperties("copilot", 60);
    CopilotCliPromptExecutor executor = new CopilotCliPromptExecutor(properties);

    Method method = CopilotCliPromptExecutor.class.getDeclaredMethod("buildPromptCommand",
        String.class, String.class, String.class);
    method.setAccessible(true);

    @SuppressWarnings("unchecked")
    List<String> command = (List<String>) method.invoke(executor, "copilot",
        "Fix the bug in main.js", "gpt-5");

    assertTrue(command.contains("--model"));
    assertTrue(command.get(command.size() - 1).equals("gpt-5"));
  }

  @Test
  void shouldDenyEveryToolSoCopilotWorksOnlyWithThePromptEvidence() throws Exception {
    CopilotCliPromptExecutor executor = new CopilotCliPromptExecutor(
        new CopilotCliProperties("copilot", 60));
    Method method = CopilotCliPromptExecutor.class.getDeclaredMethod("buildPromptCommand",
        String.class, String.class, String.class);
    method.setAccessible(true);

    @SuppressWarnings("unchecked")
    List<String> command = (List<String>) method.invoke(executor, "copilot", "prompt", "");

    assertTrue(command.containsAll(List.of("--no-custom-instructions",
        "--disable-builtin-mcps", "--no-ask-user", "--silent")));
    assertTrue(command.contains("--deny-tool=shell, write, read, url, memory"));
    assertTrue(command.stream().noneMatch(argument -> argument.startsWith("--allow")));
  }
}
