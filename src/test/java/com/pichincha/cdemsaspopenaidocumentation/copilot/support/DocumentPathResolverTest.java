package com.pichincha.cdemsaspopenaidocumentation.copilot.support;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.pichincha.cdemsaspopenaidocumentation.domain.RepositoryCoordinates;
import java.nio.file.Path;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class DocumentPathResolverTest {

  private static final RepositoryCoordinates REPOSITORY =
      new RepositoryCoordinates("org", "project", "repo");

  @Test
  void evolutionaryDocumentAndChangelogShouldLiveInTheRepositoryFolder() {
    assertEquals(Path.of("org/project/repo/error-manual.md"),
        DocumentPathResolver.evolutionaryDocument(REPOSITORY, "error-manual"));
    assertEquals(Path.of("org/project/repo/error-manual.changelog.md"),
        DocumentPathResolver.changelog(REPOSITORY, "error-manual"));
  }

  @Test
  void historicalDocumentShouldBeDatedAndTaggedWithThePullRequest() {
    Path path = DocumentPathResolver.historicalDocument(REPOSITORY, "pr-change-history",
        Instant.parse("2026-10-16T10:15:30Z"), 7L);

    assertEquals(Path.of("org/project/repo/historial/pr-change-history/20261016-101530-pr-7.md"),
        path);
  }

  @Test
  void keysShouldIdentifyEvolutionaryAndHistoricalDocuments() {
    assertEquals("org/project/repo/error-manual",
        DocumentPathResolver.evolutionaryKey(REPOSITORY, "error-manual"));
    assertEquals("org/project/repo/pr-change-history/scan-1",
        DocumentPathResolver.historicalKey(REPOSITORY, "pr-change-history", "scan-1"));
  }
}
