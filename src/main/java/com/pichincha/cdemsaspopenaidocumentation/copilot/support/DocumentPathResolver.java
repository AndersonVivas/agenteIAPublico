package com.pichincha.cdemsaspopenaidocumentation.copilot.support;

import com.pichincha.cdemsaspopenaidocumentation.domain.RepositoryCoordinates;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Resolves document keys and paths relative to the documentation output root. Evolutionary
 * documents live in {@code org/project/repo/<template>.md} with their changelog next to them;
 * historical documents are dated files under {@code org/project/repo/historial/<template>/}.
 */
public final class DocumentPathResolver {

  private static final String HISTORY_DIRECTORY = "historial";
  private static final String MARKDOWN_EXTENSION = ".md";
  private static final String CHANGELOG_EXTENSION = ".changelog.md";
  private static final DateTimeFormatter HISTORY_TIMESTAMP =
      DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").withZone(ZoneOffset.UTC);

  private DocumentPathResolver() {
  }

  public static String evolutionaryKey(RepositoryCoordinates repository, String templateId) {
    return repository.key() + "/" + RepositoryCoordinates.safeSegment(templateId);
  }

  public static String historicalKey(RepositoryCoordinates repository, String templateId,
      String scanId) {
    return evolutionaryKey(repository, templateId) + "/" + scanId;
  }

  public static Path evolutionaryDocument(RepositoryCoordinates repository, String templateId) {
    return repositoryDirectory(repository)
        .resolve(RepositoryCoordinates.safeSegment(templateId) + MARKDOWN_EXTENSION);
  }

  public static Path changelog(RepositoryCoordinates repository, String templateId) {
    return repositoryDirectory(repository)
        .resolve(RepositoryCoordinates.safeSegment(templateId) + CHANGELOG_EXTENSION);
  }

  public static Path historicalDocument(RepositoryCoordinates repository, String templateId,
      Instant generatedAt, Long pullRequestId) {
    String suffix = pullRequestId == null ? "" : "-pr-" + pullRequestId;
    return repositoryDirectory(repository).resolve(HISTORY_DIRECTORY)
        .resolve(RepositoryCoordinates.safeSegment(templateId))
        .resolve(HISTORY_TIMESTAMP.format(generatedAt) + suffix + MARKDOWN_EXTENSION);
  }

  private static Path repositoryDirectory(RepositoryCoordinates repository) {
    List<String> segments = repository.pathSegments();
    return Path.of(segments.getFirst(), segments.subList(1, segments.size())
        .toArray(String[]::new));
  }
}
