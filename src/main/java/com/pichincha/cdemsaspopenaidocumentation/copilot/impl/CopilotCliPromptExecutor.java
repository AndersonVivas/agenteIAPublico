package com.pichincha.cdemsaspopenaidocumentation.copilot.impl;

import com.pichincha.cdemsaspopenaidocumentation.copilot.PromptCommandExecutor;
import com.pichincha.cdemsaspopenaidocumentation.copilot.config.CopilotCliProperties;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.CopilotExecutionOutput;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.PromptExecutionStatus;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class CopilotCliPromptExecutor implements PromptCommandExecutor {

  private final CopilotCliProperties properties;

  public CopilotCliPromptExecutor(CopilotCliProperties properties) {
    this.properties = properties;
  }

  @Override
  public CopilotExecutionOutput execute(String prompt, Path workingDirectory) {
    try {
      Process process = startProcess(prompt, workingDirectory);
      try (ExecutorService executor = java.util.concurrent.Executors
          .newVirtualThreadPerTaskExecutor()) {
        Future<String> stdout = executor.submit(() -> read(process.getInputStream()));
        Future<String> stderr = executor.submit(() -> read(process.getErrorStream()));
        boolean finished = process.waitFor(properties.timeoutSeconds(), TimeUnit.SECONDS);
        if (!finished) {
          return timeoutResult(process, stdout, stderr);
        }
        return completedResult(process.exitValue(), stdout.get(), stderr.get());
      }
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      return new CopilotExecutionOutput("", "Copilot CLI execution interrupted.",
          PromptExecutionStatus.FAILED);
    } catch (ExecutionException exception) {
      return new CopilotExecutionOutput("", "Failed to capture Copilot CLI output: "
          + exception.getMessage(), PromptExecutionStatus.FAILED);
    } catch (IllegalStateException exception) {
      return new CopilotExecutionOutput("", exception.getMessage(),
          PromptExecutionStatus.FAILED);
    }
  }

  private Process startProcess(String prompt, Path workingDirectory) {
    StringBuilder failures = new StringBuilder();
    for (String candidate : commandCandidates()) {
      List<String> command = buildPromptCommand(candidate, prompt);
      ProcessBuilder builder = new ProcessBuilder(command);
      builder.directory(workingDirectory.toFile());
      try {
        return builder.start();
      } catch (IOException exception) {
        appendFailure(failures, command,
            StringUtils.defaultIfBlank(exception.getMessage(), "unknown"));
      }
    }
    throw new IllegalStateException("Unable to execute GitHub Copilot CLI. "
        + failures);
  }

  private List<String> buildPromptCommand(String commandName, String prompt) {
    List<String> command = new ArrayList<>();
    command.add(commandName);
    command.addAll(properties.arguments());
    command.add(prompt);
    return command;
  }

  private List<String> commandCandidates() {
    Set<String> candidates = new LinkedHashSet<>();
    candidates.add(properties.command());
    candidates.add("gh");
    candidates.add("github-copilot-cli");
    candidates.add("copilot");
    return List.copyOf(candidates);
  }

  private void appendFailure(StringBuilder failures, List<String> command,
      String error) {
    if (!failures.isEmpty()) {
      failures.append(" | ");
    }
    failures.append("command='")
        .append(command.stream().limit(7).collect(Collectors.joining(" ")))
        .append("' cause='")
        .append(StringUtils.defaultIfBlank(error, "unknown"))
        .append("'");
  }

  private CopilotExecutionOutput timeoutResult(Process process, Future<String> stdout,
      Future<String> stderr) {
    process.destroyForcibly();
    return new CopilotExecutionOutput(safeGet(stdout),
        "Execution timed out after " + properties.timeoutSeconds() + " seconds.\n"
            + safeGet(stderr),
        PromptExecutionStatus.TIMEOUT);
  }

  private CopilotExecutionOutput completedResult(int exitCode, String stdout,
      String stderr) {
    PromptExecutionStatus status = exitCode == 0
        ? PromptExecutionStatus.SUCCESS : PromptExecutionStatus.FAILED;
    return new CopilotExecutionOutput(stdout, stderr, status);
  }

  private String read(InputStream source) throws IOException {
    byte[] bytes = source.readAllBytes();
    return StringUtils.defaultString(new String(bytes, StandardCharsets.UTF_8));
  }

  private String safeGet(Future<String> future) {
    try {
      return future.get(50, TimeUnit.MILLISECONDS);
    } catch (Exception ignored) {
      return "";
    }
  }
}
