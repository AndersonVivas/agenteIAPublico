package com.pichincha.cdemsaspopenaidocumentation.helper;

import com.pichincha.cdemsaspopenaidocumentation.exception.RepositoryCloneException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;

@Component
public class DefaultGitCommandExecutor implements GitCommandExecutor {

  @Override
  public String execute(List<String> command, Path workingDirectory,
      Duration timeout) {
    ProcessBuilder processBuilder = new ProcessBuilder(command);
    processBuilder.directory(workingDirectory.toFile());
    processBuilder.redirectErrorStream(true);
    try {
      Process process = processBuilder.start();
      String output = waitForCompletion(process, timeout);
      int exitCode = process.exitValue();
      if (exitCode != 0) {
        throw new RepositoryCloneException(buildMessage(command, output));
      }
      return output;
    } catch (IOException exception) {
      throw new RepositoryCloneException("Unable to execute git command.", exception);
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      throw new RepositoryCloneException("Git command interrupted.", exception);
    } catch (ExecutionException exception) {
      throw new RepositoryCloneException("Unable to read git command output.", exception);
    }
  }

  private String waitForCompletion(Process process, Duration timeout)
      throws InterruptedException, ExecutionException {
    try (ExecutorService executor = java.util.concurrent.Executors
        .newVirtualThreadPerTaskExecutor()) {
      Future<String> output = executor.submit(() -> readOutput(process));
      if (!process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
        process.destroyForcibly();
        throw new RepositoryCloneException("Git command timed out after " + timeout + ".");
      }
      return output.get();
    }
  }

  private String readOutput(Process process) throws IOException {
    return new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
  }

  private static String buildMessage(List<String> command, String output) {
    return "Git command failed: " + redactCredentials(String.join(" ", command))
        + " -> " + redactCredentials(output.trim());
  }

  private static String redactCredentials(String value) {
    return value.replaceAll("(https?://)([^:\\s]+):([^@\\s]+)@", "$1***:***@");
  }
}