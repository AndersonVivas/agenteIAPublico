package com.pichincha.cdemsaspopenaidocumentation.domain;

import com.pichincha.cdemsaspopenaidocumentation.domain.enums.DocumentGenerationStatusEnum;
import com.pichincha.cdemsaspopenaidocumentation.domain.enums.DocumentLifecycleEnum;
import java.util.List;

public record DocumentGenerationResult(
    String templateId,
    DocumentLifecycleEnum lifecycle,
    DocumentGenerationStatusEnum status,
    String documentPath,
    Integer documentVersion,
    List<String> updatedSections,
    long executionTimeMs,
    String message) {

  public DocumentGenerationResult {
    updatedSections = updatedSections == null ? List.of() : List.copyOf(updatedSections);
  }

  public static DocumentGenerationResult failed(String templateId, String message) {
    return new DocumentGenerationResult(templateId, null, DocumentGenerationStatusEnum.FAILED,
        null, null, List.of(), 0L, message);
  }
}
