package com.pichincha.cdemsaspopenaidocumentation.copilot.domain;

import com.pichincha.cdemsaspopenaidocumentation.domain.enums.DocumentLifecycleEnum;
import com.pichincha.cdemsaspopenaidocumentation.domain.enums.ScanScopeEnum;
import java.time.Instant;
import java.util.List;

public record ChangelogEntry(
    String documentKey,
    String repositoryKey,
    String templateId,
    DocumentLifecycleEnum lifecycle,
    int version,
    String documentPath,
    String scanId,
    Long pullRequestId,
    String headCommit,
    ScanScopeEnum scanScope,
    int changedFiles,
    int evidenceAdded,
    int evidenceRemoved,
    List<String> updatedSections,
    List<String> warnings,
    Instant createdAt) {

  public ChangelogEntry {
    updatedSections = updatedSections == null ? List.of() : List.copyOf(updatedSections);
    warnings = warnings == null ? List.of() : List.copyOf(warnings);
  }
}
