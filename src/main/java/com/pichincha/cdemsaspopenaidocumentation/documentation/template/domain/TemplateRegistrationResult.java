package com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain;

import java.util.List;
import java.util.Map;

public record TemplateRegistrationResult(
    int discoveredCount,
    int validCount,
    int publishedCount,
    int rejectedCount,
    List<String> publishedTemplateIds,
    Map<String, List<String>> validationErrors) {

  public TemplateRegistrationResult {
    publishedTemplateIds = publishedTemplateIds == null ? List.of()
        : List.copyOf(publishedTemplateIds);
    validationErrors = validationErrors == null ? Map.of()
        : Map.copyOf(validationErrors);
  }
}
