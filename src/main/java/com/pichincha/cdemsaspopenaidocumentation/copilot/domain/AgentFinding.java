package com.pichincha.cdemsaspopenaidocumentation.copilot.domain;

import java.util.Comparator;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

public record AgentFinding(
    String findingId,
    String title,
    String businessSummary,
    String technicalSummary,
    String severity,
    List<String> evidenceIds) {

  public AgentFinding {
    findingId = normalize(findingId);
    title = normalize(title);
    businessSummary = normalize(businessSummary);
    technicalSummary = normalize(technicalSummary);
    severity = normalize(severity);
    evidenceIds = normalizedIds(evidenceIds);
  }

  private static String normalize(String value) {
    return StringUtils.defaultString(value).trim();
  }

  private static List<String> normalizedIds(List<String> ids) {
    if (ids == null || ids.isEmpty()) {
      return List.of();
    }
    return ids.stream().map(AgentFinding::normalize).filter(StringUtils::isNotBlank)
        .distinct().sorted(Comparator.naturalOrder()).toList();
  }
}
