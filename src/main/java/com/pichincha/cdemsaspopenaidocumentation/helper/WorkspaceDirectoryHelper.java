package com.pichincha.cdemsaspopenaidocumentation.helper;

import com.pichincha.cdemsaspopenaidocumentation.config.GitCloneProperties;
import com.pichincha.cdemsaspopenaidocumentation.domain.RepositoryCoordinates;
import com.pichincha.cdemsaspopenaidocumentation.exception.RepositoryCloneException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WorkspaceDirectoryHelper {

  private final GitCloneProperties properties;

  public Path prepare(RepositoryCoordinates repository, String scanId) {
    Path directory = workspaceDirectory(repository, scanId);
    delete(directory);
    createParentDirectories(directory);
    return directory;
  }

  public void delete(Path directory) {
    if (directory == null || !Files.exists(directory)) {
      return;
    }
    try (Stream<Path> walk = Files.walk(directory)) {
      walk.sorted(Comparator.reverseOrder()).forEach(WorkspaceDirectoryHelper::deletePath);
    } catch (IOException | UncheckedIOException exception) {
      throw new RepositoryCloneException("Unable to clean workspace " + directory, exception);
    }
  }

  private Path workspaceDirectory(RepositoryCoordinates repository, String scanId) {
    Path directory = properties.cloneDirectory();
    for (String segment : repository.pathSegments()) {
      directory = directory.resolve(segment);
    }
    return directory.resolve(RepositoryCoordinates.safeSegment(scanId));
  }

  private void createParentDirectories(Path directory) {
    try {
      Files.createDirectories(directory.getParent());
    } catch (IOException exception) {
      throw new RepositoryCloneException("Unable to create workspace " + directory, exception);
    }
  }

  private static void deletePath(Path path) {
    try {
      Files.deleteIfExists(path);
    } catch (IOException exception) {
      throw new UncheckedIOException(exception);
    }
  }
}
