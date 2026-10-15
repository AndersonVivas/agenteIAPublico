package com.pichincha.cdemsaspopenaidocumentation.copilot.domain;

import java.util.List;
import org.apache.commons.lang3.StringUtils;

public record AgentKnowledgeResult(
    DocumentationAgentType agentType,
    List<AgentFinding> findings,
    List<String> gaps,
    List<String> warnings,
    String modelUsed) {

  public AgentKnowledgeResult {
    if (agentType == null) {
      throw new IllegalArgumentException("Agent type is required.");
    }
    findings = findings == null ? List.of() : List.copyOf(findings);
    gaps = normalizedTexts(gaps);
    warnings = normalizedTexts(warnings);
    modelUsed = StringUtils.defaultString(modelUsed).trim();
  }

  private static List<String> normalizedTexts(List<String> values) {
    if (values == null || values.isEmpty()) {
      return List.of();
    }
    return values.stream().map(StringUtils::defaultString).map(String::trim)
        .filter(StringUtils::isNotBlank).distinct().toList();
  }
}
