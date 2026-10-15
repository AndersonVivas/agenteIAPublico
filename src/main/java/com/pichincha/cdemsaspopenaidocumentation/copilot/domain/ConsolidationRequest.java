package com.pichincha.cdemsaspopenaidocumentation.copilot.domain;

import java.util.List;

public record ConsolidationRequest(
    DocumentationGenerationContext generationContext,
    List<AgentKnowledgeResult> specialistResults,
    List<String> knowledgeGaps,
    List<String> knowledgeConflicts) {

  public ConsolidationRequest {
    if (generationContext == null) {
      throw new IllegalArgumentException("Generation context is required.");
    }
    specialistResults = specialistResults == null ? List.of() : List.copyOf(specialistResults);
    knowledgeGaps = knowledgeGaps == null ? List.of() : List.copyOf(knowledgeGaps);
    knowledgeConflicts = knowledgeConflicts == null ? List.of() : List.copyOf(knowledgeConflicts);
  }
}
