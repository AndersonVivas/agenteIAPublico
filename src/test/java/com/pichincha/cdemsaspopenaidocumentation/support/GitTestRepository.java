package com.pichincha.cdemsaspopenaidocumentation.support;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class GitTestRepository {

  private final Path directory;

  private GitTestRepository(Path directory) {
    this.directory = directory;
  }

  public static GitTestRepository init(Path directory) throws IOException {
    Files.createDirectories(directory);
    GitTestRepository repository = new GitTestRepository(directory);
    repository.git("init", "-q", "-b", "main");
    repository.git("config", "user.email", "agent@test.local");
    repository.git("config", "user.name", "Agent Test");
    return repository;
  }

  public GitTestRepository write(String relativePath, String content) throws IOException {
    Path file = directory.resolve(relativePath);
    Files.createDirectories(file.getParent());
    Files.writeString(file, content);
    return this;
  }

  public GitTestRepository delete(String relativePath) throws IOException {
    Files.delete(directory.resolve(relativePath));
    return this;
  }

  public String commit(String message) throws IOException {
    git("add", "-A");
    git("commit", "-q", "-m", message);
    return git("rev-parse", "HEAD").trim();
  }

  public void createPullRequestRef(long pullRequestId) throws IOException {
    git("update-ref", "refs/pull/" + pullRequestId + "/merge", "HEAD");
  }

  public Path directory() {
    return directory;
  }

  private String git(String... arguments) throws IOException {
    List<String> command = new ArrayList<>(List.of("git"));
    command.addAll(List.of(arguments));
    Process process = new ProcessBuilder(command).directory(directory.toFile())
        .redirectErrorStream(true).start();
    try {
      String output = new String(process.getInputStream().readAllBytes(),
          StandardCharsets.UTF_8);
      if (!process.waitFor(30, TimeUnit.SECONDS) || process.exitValue() != 0) {
        throw new IOException("git " + String.join(" ", arguments) + " failed: " + output);
      }
      return output;
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      throw new IOException("git command interrupted", exception);
    }
  }
}
