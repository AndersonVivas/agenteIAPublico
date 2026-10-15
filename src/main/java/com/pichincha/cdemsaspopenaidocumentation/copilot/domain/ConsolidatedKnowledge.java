package com.pichincha.cdemsaspopenaidocumentation.copilot.domain;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public record ConsolidatedKnowledge(
    List<AgentFinding> findings,
    Map<DocumentationAgentType, List<AgentFinding>> findingsByAgent,
    List<String> unresolvedConflicts,
    List<String> warnings) {

  public ConsolidatedKnowledge {
    findings = findings == null ? List.of() : List.copyOf(findings);
    findingsByAgent = normalizeByAgent(findingsByAgent);
    unresolvedConflicts = normalizedTexts(unresolvedConflicts);
    warnings = normalizedTexts(warnings);
  }

  public List<AgentFinding> findingsFor(DocumentationAgentType agentType) {
    return findingsByAgent.getOrDefault(agentType, List.of());
  }

  public static ConsolidatedKnowledge empty() {
    return new ConsolidatedKnowledge(List.of(), Map.of(), List.of(), List.of());
  }

  private static Map<DocumentationAgentType, List<AgentFinding>> normalizeByAgent(
      Map<DocumentationAgentType, List<AgentFinding>> source) {
    Map<DocumentationAgentType, List<AgentFinding>> normalized =
        new EnumMap<>(DocumentationAgentType.class);
    if (source != null) {
      source.forEach((type, values) -> normalized.put(type,
          values == null ? List.of() : List.copyOf(values)));
    }
    return Map.copyOf(normalized);
  }

  private static List<String> normalizedTexts(List<String> values) {
    if (values == null || values.isEmpty()) {
      return List.of();
    }
    return values.stream().map(StringUtils::defaultString).map(String::trim)
        .filter(StringUtils::isNotBlank).distinct().toList();
  }
}
