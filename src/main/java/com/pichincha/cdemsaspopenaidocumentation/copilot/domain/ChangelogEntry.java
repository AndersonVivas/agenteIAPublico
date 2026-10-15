package com.pichincha.cdemsaspopenaidocumentation.copilot.domain;

import java.time.Instant;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

public record ChangelogEntry(
    Instant timestamp,
    String repositoryName,
    String commitHash,
    Long pullRequestId,
    List<String> detectedChanges,
    List<String> updatedSections,
    List<String> newEvidenceIds) {

  public ChangelogEntry {
    timestamp = timestamp == null ? Instant.now() : timestamp;
    repositoryName = StringUtils.defaultString(repositoryName).trim();
    commitHash = StringUtils.defaultIfBlank(commitHash, "unknown").trim();
    detectedChanges = normalized(detectedChanges);
    updatedSections = normalized(updatedSections);
    newEvidenceIds = normalized(newEvidenceIds);
  }

  private static List<String> normalized(List<String> values) {
    if (values == null || values.isEmpty()) {
      return List.of();
    }
    return values.stream().map(StringUtils::defaultString).map(String::trim)
        .filter(StringUtils::isNotBlank).distinct().toList();
  }
}
