package com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.domain;

import com.pichincha.cdemsaspopenaidocumentation.documentation.communication.domain.PresentationMode;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.EvidenceRef;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

public record SectionContext(
    String sectionId,
    String title,
    String objective,
    List<String> instructions,
    boolean required,
    int order,
    String narrativeIntent,
    PresentationMode presentationMode,
    List<String> highlights,
    List<EvidenceRef> traceabilityRefs,
    List<String> warnings,
    String emptyStateMessage) {

  public SectionContext {
    sectionId = normalize(sectionId);
    title = normalize(title);
    objective = normalize(objective);
    instructions = instructions == null ? List.of() : List.copyOf(instructions);
    narrativeIntent = normalize(narrativeIntent);
    presentationMode = presentationMode == null
        ? PresentationMode.SUMMARY_FIRST : presentationMode;
    highlights = highlights == null ? List.of() : List.copyOf(highlights);
    traceabilityRefs = traceabilityRefs == null ? List.of() : List.copyOf(traceabilityRefs);
    warnings = warnings == null ? List.of() : List.copyOf(warnings);
    emptyStateMessage = normalize(emptyStateMessage);
  }

  private static String normalize(String value) {
    return StringUtils.defaultString(value).trim();
  }
}
