package com.pichincha.cdemsaspopenaidocumentation.copilot.domain;

import java.util.List;
import org.apache.commons.lang3.StringUtils;

public record ConsolidatedKnowledge(
    List<AgentFinding> findings,
    List<String> unresolvedConflicts,
    List<String> warnings) {

  public ConsolidatedKnowledge {
    findings = findings == null ? List.of() : List.copyOf(findings);
    unresolvedConflicts = normalizedTexts(unresolvedConflicts);
    warnings = normalizedTexts(warnings);
  }

  private static List<String> normalizedTexts(List<String> values) {
    if (values == null || values.isEmpty()) {
      return List.of();
    }
    return values.stream().map(StringUtils::defaultString).map(String::trim)
        .filter(StringUtils::isNotBlank).distinct().toList();
  }
}
