package com.pichincha.cdemsaspopenaidocumentation.copilot.impl;

import com.pichincha.cdemsaspopenaidocumentation.copilot.PromptCommandExecutor;
import com.pichincha.cdemsaspopenaidocumentation.copilot.config.CopilotCliProperties;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.CopilotExecutionOutput;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.PromptExecutionStatus;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CopilotCliPromptExecutor implements PromptCommandExecutor {

  private static final String PROMPT_ARGUMENT = "-p";
  private static final List<String> ISOLATION_ARGUMENTS = List.of(
      "--silent",
      "--no-ask-user",
      "--no-custom-instructions",
      "--disable-builtin-mcps",
      "--deny-tool=shell, write, read, url, memory");
  private static final long OUTPUT_DRAIN_MILLIS = 50;

  private final CopilotCliProperties properties;

  @Override
  public CopilotExecutionOutput execute(String prompt, String model) {
    Path isolatedDirectory = createIsolatedDirectory();
    try {
      return awaitCompletion(startProcess(prompt, model, isolatedDirectory));
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      return failed("Copilot CLI execution interrupted.");
    } catch (ExecutionException exception) {
      return failed("Failed to capture Copilot CLI output: " + exception.getMessage());
    } catch (IllegalStateException exception) {
      return failed(exception.getMessage());
    } finally {
      deleteDirectory(isolatedDirectory);
    }
  }

  private CopilotExecutionOutput awaitCompletion(Process process)
      throws InterruptedException, ExecutionException {
    try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
      Future<String> stdout = executor.submit(() -> read(process.getInputStream()));
      Future<String> stderr = executor.submit(() -> read(process.getErrorStream()));
      if (!process.waitFor(properties.timeoutSeconds(), TimeUnit.SECONDS)) {
        return timeoutResult(process, stdout, stderr);
      }
      return completedResult(process.exitValue(), stdout.get(), stderr.get());
    }
  }

  private Path createIsolatedDirectory() {
    try {
      return Files.createTempDirectory("copilot-execution");
    } catch (IOException exception) {
      throw new IllegalStateException("Failed to create isolated execution directory.",
          exception);
    }
  }

  private Process startProcess(String prompt, String model, Path workingDirectory) {
    StringBuilder failures = new StringBuilder();
    for (String candidate : commandCandidates()) {
      List<String> command = buildPromptCommand(candidate, prompt, model);
      ProcessBuilder builder = new ProcessBuilder(command);
      builder.directory(workingDirectory.toFile());
      hardenEnvironment(builder.environment());
      try {
        return builder.start();
      } catch (IOException exception) {
        appendFailure(failures, command,
            StringUtils.defaultIfBlank(exception.getMessage(), "unknown"));
      }
    }
    throw new IllegalStateException("Unable to execute GitHub Copilot CLI. " + failures);
  }

  private void hardenEnvironment(Map<String, String> environment) {
    environment.remove("GIT_DIR");
    environment.remove("GIT_WORK_TREE");
    environment.remove("PWD");
  }

  private List<String> buildPromptCommand(String commandName, String prompt, String model) {
    List<String> command = new ArrayList<>();
    command.add(commandName);
    command.addAll(ISOLATION_ARGUMENTS);
    command.add(PROMPT_ARGUMENT);
    command.add(prompt);
    if (StringUtils.isNotBlank(model)) {
      command.add("--model");
      command.add(model);
    }
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

  private void appendFailure(StringBuilder failures, List<String> command, String error) {
    if (!failures.isEmpty()) {
      failures.append(" | ");
    }
    failures.append("command='")
        .append(command.stream().limit(2).collect(Collectors.joining(" ")))
        .append("' cause='").append(error).append("'");
  }

  private CopilotExecutionOutput timeoutResult(Process process, Future<String> stdout,
      Future<String> stderr) {
    process.destroyForcibly();
    return new CopilotExecutionOutput(drain(stdout),
        "Execution timed out after " + properties.timeoutSeconds() + " seconds.\n"
            + drain(stderr),
        PromptExecutionStatus.TIMEOUT);
  }

  private CopilotExecutionOutput completedResult(int exitCode, String stdout, String stderr) {
    PromptExecutionStatus status = exitCode == 0
        ? PromptExecutionStatus.SUCCESS : PromptExecutionStatus.FAILED;
    return new CopilotExecutionOutput(stdout, stderr, status);
  }

  private CopilotExecutionOutput failed(String message) {
    return new CopilotExecutionOutput("", message, PromptExecutionStatus.FAILED);
  }

  private String read(InputStream source) throws IOException {
    return new String(source.readAllBytes(), StandardCharsets.UTF_8);
  }

  private String drain(Future<String> future) {
    try {
      return future.get(OUTPUT_DRAIN_MILLIS, TimeUnit.MILLISECONDS);
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      return "";
    } catch (ExecutionException | TimeoutException exception) {
      return "";
    }
  }

  private void deleteDirectory(Path directory) {
    try (Stream<Path> walk = Files.walk(directory)) {
      walk.sorted(Comparator.reverseOrder()).forEach(this::deletePath);
    } catch (IOException | UncheckedIOException exception) {
      log.warn("Unable to delete Copilot isolated directory {}", directory, exception);
    }
  }

  private void deletePath(Path path) {
    try {
      Files.deleteIfExists(path);
    } catch (IOException exception) {
      throw new UncheckedIOException(exception);
    }
  }
}
