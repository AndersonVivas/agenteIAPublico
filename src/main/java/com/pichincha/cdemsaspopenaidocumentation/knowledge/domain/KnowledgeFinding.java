package com.pichincha.cdemsaspopenaidocumentation.knowledge.domain;

import java.util.Comparator;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

public record KnowledgeFinding(
    String findingId,
    String category,
    String normalizedKey,
    String title,
    String summary,
    String severity,
    double confidence,
    List<EvidenceRef> evidenceRefs) {

  public KnowledgeFinding {
    findingId = normalize(findingId);
    category = normalize(category);
    normalizedKey = normalize(normalizedKey);
    title = normalize(title);
    summary = normalize(summary);
    severity = normalize(severity);
    evidenceRefs = normalizedEvidence(evidenceRefs);
  }

  private static String normalize(String value) {
    return StringUtils.defaultString(value).trim();
  }

  private static List<EvidenceRef> normalizedEvidence(List<EvidenceRef> evidenceRefs) {
    if (evidenceRefs == null || evidenceRefs.isEmpty()) {
      return List.of();
    }
    return evidenceRefs.stream().distinct()
        .sorted(Comparator.comparing(EvidenceRef::evidenceId)).toList();
  }
}
