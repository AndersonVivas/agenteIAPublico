package com.pichincha.cdemsaspopenaidocumentation.helper;

import com.pichincha.cdemsaspopenaidocumentation.config.GitCloneProperties;
import com.pichincha.cdemsaspopenaidocumentation.domain.RepositoryCloneResult;
import com.pichincha.cdemsaspopenaidocumentation.domain.ScanCommand;
import com.pichincha.cdemsaspopenaidocumentation.exception.RepositoryCloneException;
import java.nio.file.Path;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GitRepositoryCloner {

  private static final String PULL_REQUEST_MERGE_REF = "refs/pull/%d/merge";

  private final GitCloneProperties properties;
  private final GitCommandExecutor commandExecutor;
  private final WorkspaceDirectoryHelper workspaceDirectoryHelper;

  public RepositoryCloneResult cloneRepository(ScanCommand command) {
    Path directory = workspaceDirectoryHelper.prepare(command.repository(), command.scanId());
    run(List.of("git", "clone", "--depth", "1",
        properties.repositoryUrl(command.repository()), directory.toString()),
        directory.getParent());
    boolean pullRequestResolved = checkoutPullRequest(command.pullRequestId(), directory);
    String checkedOutRef = pullRequestResolved
        ? PULL_REQUEST_MERGE_REF.formatted(command.pullRequestId()) : currentBranch(directory);
    return new RepositoryCloneResult(directory, headCommit(directory), checkedOutRef,
        pullRequestResolved);
  }

  public void release(RepositoryCloneResult cloneResult) {
    workspaceDirectoryHelper.delete(cloneResult.localPath());
  }

  private boolean checkoutPullRequest(Long pullRequestId, Path directory) {
    if (pullRequestId == null) {
      return false;
    }
    try {
      run(List.of("git", "fetch", "--depth", "1", "origin",
          PULL_REQUEST_MERGE_REF.formatted(pullRequestId)), directory);
      run(List.of("git", "checkout", "--detach", "FETCH_HEAD"), directory);
      return true;
    } catch (RepositoryCloneException exception) {
      log.warn("Pull request {} merge ref is not available, scanning the default branch: {}",
          pullRequestId, exception.getMessage());
      return false;
    }
  }

  private String currentBranch(Path directory) {
    return run(List.of("git", "rev-parse", "--abbrev-ref", "HEAD"), directory).trim();
  }

  private String headCommit(Path directory) {
    return run(List.of("git", "rev-parse", "HEAD"), directory).trim();
  }

  private String run(List<String> command, Path workingDirectory) {
    return commandExecutor.execute(command, workingDirectory, properties.cloneTimeout());
  }
}
