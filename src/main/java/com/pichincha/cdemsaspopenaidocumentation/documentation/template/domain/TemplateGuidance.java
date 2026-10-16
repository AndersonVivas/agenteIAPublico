package com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain;

import com.pichincha.cdemsaspopenaidocumentation.domain.enums.DocumentLifecycleEnum;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

public record TemplateGuidance(
    DocumentLifecycleEnum lifecycle,
    List<String> globalRules,
    String missingDataMessage,
    List<String> skills,
    boolean editorialReview) {

  private static final String DEFAULT_MISSING_DATA_MESSAGE = "No evidenciado en el escaneo.";

  public TemplateGuidance {
    lifecycle = lifecycle == null ? DocumentLifecycleEnum.EVOLUTIONARY : lifecycle;
    globalRules = globalRules == null ? List.of() : globalRules.stream()
        .map(StringUtils::defaultString).map(String::trim)
        .filter(StringUtils::isNotBlank).toList();
    missingDataMessage = StringUtils.defaultIfBlank(missingDataMessage,
        DEFAULT_MISSING_DATA_MESSAGE).trim();
    skills = skills == null ? List.of() : skills.stream().map(StringUtils::defaultString)
        .map(String::trim).filter(StringUtils::isNotBlank).distinct().toList();
  }

  public static TemplateGuidance defaults() {
    return new TemplateGuidance(null, List.of(), null, List.of(), false);
  }
}
