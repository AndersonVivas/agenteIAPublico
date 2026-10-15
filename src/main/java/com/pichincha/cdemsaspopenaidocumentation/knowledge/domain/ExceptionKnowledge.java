package com.pichincha.cdemsaspopenaidocumentation.knowledge.domain;

import java.util.Comparator;
import java.util.List;

public record ExceptionKnowledge(List<KnowledgeFinding> findings) {

  public ExceptionKnowledge {
    findings = findings == null ? List.of() : findings.stream().distinct()
        .sorted(Comparator.comparing(KnowledgeFinding::findingId)).toList();
  }
}
