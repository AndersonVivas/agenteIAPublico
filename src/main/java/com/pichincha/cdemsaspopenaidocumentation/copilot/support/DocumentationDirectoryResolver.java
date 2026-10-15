package com.pichincha.cdemsaspopenaidocumentation.copilot.support;

import java.nio.file.Path;
import org.apache.commons.lang3.StringUtils;

/**
 * Resolves the on-disk directory a generated document (or its sidecar state/changelog
 * files) lives in, honoring the same centralized-output-path-wins-over-repository-relative
 * rule used by {@code FileSystemDocumentPersistenceService}.
 */
public final class DocumentationDirectoryResolver {

  private DocumentationDirectoryResolver() {
  }

  public static Path resolve(String repositoryPath, String outputDirectory,
      String centralizedOutputPath) {
    if (StringUtils.isNotBlank(centralizedOutputPath)) {
      return Path.of(centralizedOutputPath);
    }
    return Path.of(repositoryPath).resolve(outputDirectory);
  }
}
