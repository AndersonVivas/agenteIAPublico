package com.pichincha.cdemsaspopenaidocumentation.copilot;

import java.nio.file.Path;

public interface DocumentPersistenceService {

  /**
   * Writes the content to a path relative to the documentation output root and returns the
   * absolute location of the written file.
   */
  Path save(Path relativePath, String content);
}
