package com.pichincha.cdemsaspopenaidocumentation.knowledge.domain;

import java.util.Comparator;
import java.util.List;

public record MonitoringKnowledge(List<KnowledgeFinding> findings) {

  public MonitoringKnowledge {
    findings = findings == null ? List.of() : findings.stream().distinct()
        .sorted(Comparator.comparing(KnowledgeFinding::findingId)).toList();
  }
}
