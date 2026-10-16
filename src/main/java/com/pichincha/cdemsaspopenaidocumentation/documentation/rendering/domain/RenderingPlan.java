package com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.domain;

import java.util.List;

public record RenderingPlan(
    List<SectionContext> sectionContexts,
    List<String> knowledgeGaps,
    List<String> knowledgeConflicts,
    List<String> warnings) {

  public RenderingPlan {
    sectionContexts = sectionContexts == null ? List.of() : List.copyOf(sectionContexts);
    knowledgeGaps = knowledgeGaps == null ? List.of() : List.copyOf(knowledgeGaps);
    knowledgeConflicts = knowledgeConflicts == null ? List.of() : List.copyOf(knowledgeConflicts);
    warnings = warnings == null ? List.of() : List.copyOf(warnings);
  }
}
