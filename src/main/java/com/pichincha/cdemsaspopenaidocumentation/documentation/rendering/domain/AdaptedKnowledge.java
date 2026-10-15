package com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.domain;

import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.KnowledgeConflict;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.KnowledgeGap;
import java.util.List;

public record AdaptedKnowledge(
    List<AdaptedFinding> findings,
    List<KnowledgeGap> gaps,
    List<KnowledgeConflict> conflicts,
    List<String> warnings) {

  public AdaptedKnowledge {
    findings = findings == null ? List.of() : List.copyOf(findings);
    gaps = gaps == null ? List.of() : List.copyOf(gaps);
    conflicts = conflicts == null ? List.of() : List.copyOf(conflicts);
    warnings = warnings == null ? List.of() : List.copyOf(warnings);
  }
}
