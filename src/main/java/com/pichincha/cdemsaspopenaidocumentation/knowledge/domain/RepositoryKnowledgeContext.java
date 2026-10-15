package com.pichincha.cdemsaspopenaidocumentation.knowledge.domain;

import java.time.Instant;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public record RepositoryKnowledgeContext(
    String scanId,
    String repositoryFingerprint,
    Instant scanTimestamp,
    long totalFiles,
    long directories,
    long signalsExtracted,
    Map<String, Long> filesByLanguage,
    Map<String, Long> filesByExtension) {

  public RepositoryKnowledgeContext {
    scanId = normalize(scanId);
    repositoryFingerprint = normalize(repositoryFingerprint);
    scanTimestamp = scanTimestamp == null ? Instant.EPOCH : scanTimestamp;
    filesByLanguage = filesByLanguage == null ? Map.of() : Map.copyOf(filesByLanguage);
    filesByExtension = filesByExtension == null ? Map.of() : Map.copyOf(filesByExtension);
  }

  private static String normalize(String value) {
    return StringUtils.defaultString(value).trim();
  }
}
