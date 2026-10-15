package com.pichincha.cdemsaspopenaidocumentation.knowledge.domain;

import java.util.Comparator;
import java.util.List;

public record ApiKnowledge(List<KnowledgeFinding> findings) {

  public ApiKnowledge {
    findings = findings == null ? List.of() : findings.stream().distinct()
        .sorted(Comparator.comparing(KnowledgeFinding::findingId)).toList();
  }
}
