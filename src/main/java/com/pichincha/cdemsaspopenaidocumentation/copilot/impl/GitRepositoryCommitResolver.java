package com.pichincha.cdemsaspopenaidocumentation.copilot.impl;

import com.pichincha.cdemsaspopenaidocumentation.copilot.RepositoryCommitResolver;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class GitRepositoryCommitResolver implements RepositoryCommitResolver {

  @Override
  public Optional<String> resolveCommitHash(Path repositoryPath) {
    ProcessBuilder builder = new ProcessBuilder(List.of("git", "rev-parse", "HEAD"));
    builder.directory(repositoryPath.toFile());
    builder.redirectErrorStream(true);
    try {
      Process process = builder.start();
      String output = readOutput(process);
      int exitCode = process.waitFor();
      return exitCode == 0 ? Optional.of(output.trim()) : Optional.empty();
    } catch (IOException exception) {
      return Optional.empty();
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      return Optional.empty();
    }
  }

  private String readOutput(Process process) throws IOException {
    byte[] content = process.getInputStream().readAllBytes();
    return StringUtils.defaultString(new String(content, StandardCharsets.UTF_8));
  }
}
