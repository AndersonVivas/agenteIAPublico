package com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.domain;

import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.EvidenceRef;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

public record AdaptedFinding(
    String findingId,
    String category,
    String severity,
    String adaptedTitle,
    String adaptedSummary,
    List<EvidenceRef> evidenceRefs) {

  public AdaptedFinding {
    findingId = normalize(findingId);
    category = normalize(category);
    severity = normalize(severity);
    adaptedTitle = normalize(adaptedTitle);
    adaptedSummary = normalize(adaptedSummary);
    evidenceRefs = evidenceRefs == null ? List.of() : List.copyOf(evidenceRefs);
  }

  private static String normalize(String value) {
    return StringUtils.defaultString(value).trim();
  }
}
