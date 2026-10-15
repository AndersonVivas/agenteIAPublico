package com.pichincha.cdemsaspopenaidocumentation.copilot;

import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentStateSnapshot;
import java.util.Optional;

/**
 * Persists the per-section content fingerprints of the last generated document so the
 * next execution can detect which sections changed and only regenerate those.
 */
public interface DocumentStateRepository {

  Optional<DocumentStateSnapshot> load(String repositoryPath, String outputDirectory,
      String centralizedOutputPath, String documentName);

  void save(String repositoryPath, String outputDirectory, String centralizedOutputPath,
      String documentName, DocumentStateSnapshot snapshot);
}
