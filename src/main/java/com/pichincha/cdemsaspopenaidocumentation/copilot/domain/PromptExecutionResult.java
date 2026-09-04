package com.pichincha.cdemsaspopenaidocumentation.copilot.domain;

import java.time.Instant;
import org.apache.commons.lang3.StringUtils;

public record PromptExecutionResult(
    String executionId,
    String generatedContent,
    String documentPath,
    String documentName,
    PromptExecutionStatus executionStatus,
    long executionTime,
    String provider,
    Instant timestamp) {

  public PromptExecutionResult {
    executionId = normalize(executionId);
    generatedContent = StringUtils.defaultString(generatedContent);
    documentPath = normalize(documentPath);
    documentName = normalize(documentName);
    provider = normalize(provider);
    timestamp = timestamp == null ? Instant.now() : timestamp;
  }

  private static String normalize(String value) {
    return StringUtils.defaultString(value).trim();
  }
}
