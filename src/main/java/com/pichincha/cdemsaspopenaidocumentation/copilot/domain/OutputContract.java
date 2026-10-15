package com.pichincha.cdemsaspopenaidocumentation.copilot.domain;

import java.util.List;
import org.apache.commons.lang3.StringUtils;

public record OutputContract(
    String outputFormat,
    String outputLanguage,
    String objective,
    String missingDataMessage,
    boolean enforceTraceability,
    List<String> mandatorySections,
    List<String> mandatoryErrorFields,
    List<String> constraints,
    List<String> rules) {

  public OutputContract {
    outputFormat = normalize(outputFormat);
    outputLanguage = normalize(outputLanguage);
    objective = normalize(objective);
    missingDataMessage = normalize(missingDataMessage);
    mandatorySections = copy(mandatorySections);
    mandatoryErrorFields = copy(mandatoryErrorFields);
    constraints = copy(constraints);
    rules = copy(rules);
  }

  private static String normalize(String value) {
    return StringUtils.defaultString(value).trim();
  }

  private static List<String> copy(List<String> values) {
    if (values == null || values.isEmpty()) {
      return List.of();
    }
    return values.stream().map(OutputContract::normalize)
        .filter(StringUtils::isNotBlank).toList();
  }
}
