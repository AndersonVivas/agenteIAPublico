package com.pichincha.cdemsaspopenaidocumentation.copilot;

import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.ChangelogEntry;
import java.nio.file.Path;

/**
 * Keeps the version history of every generated document and renders the changelog associated
 * with evolutionary documents, separate from the document itself.
 */
public interface ChangelogService {

  int nextVersion(String documentKey);

  void record(ChangelogEntry entry);

  void writeChangelog(String documentKey, Path changelogPath, String documentTitle);
}
