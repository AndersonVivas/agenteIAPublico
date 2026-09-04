package com.pichincha.cdemsaspopenaidocumentation.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.pichincha.cdemsaspopenaidocumentation.config.GitCloneProperties;
import com.pichincha.cdemsaspopenaidocumentation.domain.CommandExecutionResult;
import com.pichincha.cdemsaspopenaidocumentation.domain.RepositoryCloneRequest;
import com.pichincha.cdemsaspopenaidocumentation.domain.RepositoryCloneResult;
import com.pichincha.cdemsaspopenaidocumentation.exception.RepositoryCloneException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class GitRepositoryClonerTest {

  @Test
  void cloneRepositoryShouldBuildGitCloneCommand() throws Exception {
    Path cloneDirectory = Files.createTempDirectory("git-clone-test");
    CapturingGitCommandExecutor executor = new CapturingGitCommandExecutor();
    GitCloneProperties properties = new GitCloneProperties("https://dev.azure.com",
      "avivaspa", "test-pat", cloneDirectory, false);
    GitRepositoryCloner cloner = new GitRepositoryCloner(properties, executor);

    RepositoryCloneResult result = cloner.cloneRepository(
      new RepositoryCloneRequest("BancoPichinchaEC", "taa-serviciosadministrativos",
        "sad-mna-logistica-del-efectivo", 18L));

    assertEquals(List.of("git", "clone",
      "https://avivaspa:test-pat@dev.azure.com/BancoPichinchaEC/"
        + "taa-serviciosadministrativos/_git/"
        + "sad-mna-logistica-del-efectivo",
      cloneDirectory.resolve("BancoPichinchaEC")
        .resolve("taa-serviciosadministrativos")
        .resolve("sad-mna-logistica-del-efectivo")
        .toString()), executor.command);
    assertEquals(cloneDirectory, executor.workingDirectory);
    assertEquals("sad-mna-logistica-del-efectivo", result.repositoryName());
    assertEquals(18L, result.pullRequestId());
    assertEquals(cloneDirectory.resolve("BancoPichinchaEC")
      .resolve("taa-serviciosadministrativos")
      .resolve("sad-mna-logistica-del-efectivo")
      .toString(), result.localPath());
  }

  @Test
  void cloneRepositoryShouldFailWhenBaseUrlIsMissing() throws Exception {
    Path cloneDirectory = Files.createTempDirectory("git-clone-test");
    GitRepositoryCloner cloner = new GitRepositoryCloner(new GitCloneProperties("",
      "avivaspa", "test-pat", cloneDirectory, false),
      new CapturingGitCommandExecutor());

    assertThrows(RepositoryCloneException.class,
      () -> cloner.cloneRepository(new RepositoryCloneRequest("BancoPichinchaEC",
        "taa-serviciosadministrativos", "sad-mna-logistica-del-efectivo", 18L)));
  }

  @Test
  void cloneRepositoryShouldUseHttpsWhenCredentialsAreMissing() throws Exception {
    Path cloneDirectory = Files.createTempDirectory("git-clone-test");
    CapturingGitCommandExecutor executor = new CapturingGitCommandExecutor();
    GitCloneProperties properties = new GitCloneProperties("dev.azure.com", "", "",
      cloneDirectory, false);
    GitRepositoryCloner cloner = new GitRepositoryCloner(properties, executor);

    cloner.cloneRepository(new RepositoryCloneRequest("BancoPichinchaEC",
      "taa-serviciosadministrativos", "sad-msa-dm-cchd-central-cash-handling", 21L));

    assertEquals("https://dev.azure.com/BancoPichinchaEC/taa-serviciosadministrativos"
      + "/_git/sad-msa-dm-cchd-central-cash-handling", executor.command.get(2));
  }

  static class CapturingGitCommandExecutor implements GitCommandExecutor {

    private List<String> command = new ArrayList<>();
    private Path workingDirectory;

    @Override
    public CommandExecutionResult execute(List<String> command, Path workingDirectory) {
      this.command = List.copyOf(command);
      this.workingDirectory = workingDirectory;
      return new CommandExecutionResult(0, "ok");
    }
  }
}