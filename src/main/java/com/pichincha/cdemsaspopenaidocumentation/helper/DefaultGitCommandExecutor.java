package com.pichincha.cdemsaspopenaidocumentation.helper;

import com.pichincha.cdemsaspopenaidocumentation.domain.CommandExecutionResult;
import com.pichincha.cdemsaspopenaidocumentation.exception.RepositoryCloneException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DefaultGitCommandExecutor implements GitCommandExecutor {

  @Override
  public CommandExecutionResult execute(List<String> command, Path workingDirectory) {
    ProcessBuilder processBuilder = new ProcessBuilder(command);
    processBuilder.directory(workingDirectory.toFile());
    processBuilder.redirectErrorStream(true);
    try {
      Process process = processBuilder.start();
      String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
      int exitCode = process.waitFor();
      if (exitCode != 0) {
        throw new RepositoryCloneException(buildMessage(command, output));
      }
      return new CommandExecutionResult(exitCode, output);
    } catch (IOException exception) {
      throw new RepositoryCloneException("Unable to execute git command.", exception);
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      throw new RepositoryCloneException("Git command interrupted.", exception);
    }
  }

  private static String buildMessage(List<String> command, String output) {
    return "Git command failed: " + redactCredentials(String.join(" ", command))
        + " -> " + output.trim();
  }

  private static String redactCredentials(String value) {
    return value.replaceAll("(https?://)([^:\\s]+):([^@\\s]+)@", "$1***:***@");
  }
}