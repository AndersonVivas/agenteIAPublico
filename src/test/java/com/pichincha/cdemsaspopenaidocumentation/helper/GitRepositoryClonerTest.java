package com.pichincha.cdemsaspopenaidocumentation.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pichincha.cdemsaspopenaidocumentation.config.GitCloneProperties;
import com.pichincha.cdemsaspopenaidocumentation.domain.RepositoryCloneResult;
import com.pichincha.cdemsaspopenaidocumentation.domain.RepositoryCoordinates;
import com.pichincha.cdemsaspopenaidocumentation.domain.ScanCommand;
import com.pichincha.cdemsaspopenaidocumentation.support.GitTestRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GitRepositoryClonerTest {

  private static final RepositoryCoordinates REPOSITORY =
      new RepositoryCoordinates("org", "project", "repo");

  @TempDir
  private Path temp;

  private GitTestRepository remote;
  private GitRepositoryCloner cloner;

  @BeforeEach
  void setUp() throws IOException {
    remote = GitTestRepository.init(temp.resolve("remotes/org/project/repo"));
    remote.write("README.md", "base").commit("base");
    GitCloneProperties properties = new GitCloneProperties(
        temp.resolve("remotes").toUri().toString(), null, null, temp.resolve("clones"),
        Duration.ofMinutes(1));
    cloner = new GitRepositoryCloner(properties, new DefaultGitCommandExecutor(),
        new WorkspaceDirectoryHelper(properties));
  }

  @Test
  void cloneRepositoryShouldCheckoutPullRequestMergeRefWhenAvailable() throws IOException {
    remote.write("Feature.java", "class Feature {}");
    String pullRequestCommit = remote.commit("feature");
    remote.createPullRequestRef(7L);

    RepositoryCloneResult result = cloner.cloneRepository(command(7L));

    assertTrue(result.pullRequestResolved());
    assertEquals("refs/pull/7/merge", result.checkedOutRef());
    assertEquals(pullRequestCommit, result.headCommit());
    assertTrue(Files.exists(result.localPath().resolve("Feature.java")));
  }

  @Test
  void cloneRepositoryShouldFallBackToDefaultBranchWhenPullRequestRefIsMissing() {
    RepositoryCloneResult result = cloner.cloneRepository(command(99L));

    assertFalse(result.pullRequestResolved());
    assertEquals("main", result.checkedOutRef());
  }

  @Test
  void releaseShouldDeleteTheWorkspace() {
    RepositoryCloneResult result = cloner.cloneRepository(command(null));

    cloner.release(result);

    assertFalse(Files.exists(result.localPath()));
  }

  private ScanCommand command(Long pullRequestId) {
    return new ScanCommand("scan-1", REPOSITORY, pullRequestId, false);
  }
}
