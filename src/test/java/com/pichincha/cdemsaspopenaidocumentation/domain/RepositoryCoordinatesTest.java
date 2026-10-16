package com.pichincha.cdemsaspopenaidocumentation.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class RepositoryCoordinatesTest {

  @Test
  void keyShouldSanitizeUnsafeCharacters() {
    RepositoryCoordinates repository = new RepositoryCoordinates(" Org ", "my project", "repo/x");

    assertEquals("Org/my-project/repo-x", repository.key());
  }

  @Test
  void pathSegmentsShouldNeverEscapeTheWorkspace() {
    RepositoryCoordinates repository = new RepositoryCoordinates("..", ".", " ");

    assertEquals(List.of("_", "_", "_"), repository.pathSegments());
  }
}
