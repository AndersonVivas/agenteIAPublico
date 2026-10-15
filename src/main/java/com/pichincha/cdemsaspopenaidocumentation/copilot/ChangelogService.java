package com.pichincha.cdemsaspopenaidocumentation.copilot;

import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.ChangelogEntry;

/**
 * Records one execution's changes in a changelog that is kept separate from the main
 * generated document, as mandated by the evolutionary documentation strategy.
 */
public interface ChangelogService {

  void record(String repositoryPath, String outputDirectory, String centralizedOutputPath,
      ChangelogEntry entry);
}
