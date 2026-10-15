package com.pichincha.cdemsaspopenaidocumentation.knowledge.domain;

import java.util.Comparator;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

public record KnowledgeConflict(
    String conflictId,
    String category,
    String description,
    List<EvidenceRef> evidenceRefs) {

  public KnowledgeConflict {
    conflictId = normalize(conflictId);
    category = normalize(category);
    description = normalize(description);
    evidenceRefs = evidenceRefs == null ? List.of() : evidenceRefs.stream().distinct()
        .sorted(Comparator.comparing(EvidenceRef::evidenceId)).toList();
  }

  private static String normalize(String value) {
    return StringUtils.defaultString(value).trim();
  }
}
