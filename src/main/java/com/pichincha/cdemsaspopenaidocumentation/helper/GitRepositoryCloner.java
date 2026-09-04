package com.pichincha.cdemsaspopenaidocumentation.helper;

import com.pichincha.cdemsaspopenaidocumentation.config.GitCloneProperties;
import com.pichincha.cdemsaspopenaidocumentation.domain.RepositoryCloneRequest;
import com.pichincha.cdemsaspopenaidocumentation.domain.RepositoryCloneResult;
import com.pichincha.cdemsaspopenaidocumentation.exception.RepositoryCloneException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class GitRepositoryCloner {

  private final GitCloneProperties properties;
  private final GitCommandExecutor commandExecutor;

  public GitRepositoryCloner(GitCloneProperties properties,
      GitCommandExecutor commandExecutor) {
    this.properties = properties;
    this.commandExecutor = commandExecutor;
  }

  public RepositoryCloneResult cloneRepository(RepositoryCloneRequest request) {
    validateBaseUrl();
    Path targetDirectory = properties.repositoryDirectory(request.organization(),
        request.projectName(), request.repositoryName());
    prepareTargetDirectory(targetDirectory);
    executeClone(request, targetDirectory);
    return RepositoryCloneResult.cloned(request.repositoryName(), request.pullRequestId(),
        targetDirectory);
  }

  private void executeClone(RepositoryCloneRequest request, Path targetDirectory) {
    List<String> command = List.of("git", "clone",
        properties.repositoryUrl(request.organization(), request.projectName(),
            request.repositoryName()),
        targetDirectory.toString());
    commandExecutor.execute(command, properties.cloneDirectory());
  }

  private void prepareTargetDirectory(Path targetDirectory) {
    try {
      if (Files.exists(targetDirectory) && !properties.overwriteExisting()) {
        throw new RepositoryCloneException("Target directory already exists: " + targetDirectory);
      }
      deleteIfNeeded(targetDirectory);
      Files.createDirectories(targetDirectory);
    } catch (IOException exception) {
      throw new RepositoryCloneException("Unable to prepare repository directory.", exception);
    }
  }

  private void deleteIfNeeded(Path targetDirectory) throws IOException {
    if (!Files.exists(targetDirectory) || !properties.overwriteExisting()) {
      return;
    }
    Files.walk(targetDirectory)
        .sorted(Comparator.reverseOrder())
        .forEach(GitRepositoryCloner::deleteSilently);
  }

  private static void deleteSilently(Path path) {
    try {
      Files.deleteIfExists(path);
    } catch (IOException exception) {
      throw new RepositoryCloneException("Unable to clean repository directory.", exception);
    }
  }

  private void validateBaseUrl() {
    if (StringUtils.isBlank(properties.baseUrl())) {
      throw new RepositoryCloneException("app.git.base-url must be configured.");
    }
  }
}