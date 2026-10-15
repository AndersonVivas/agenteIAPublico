package com.pichincha.cdemsaspopenaidocumentation.copilot.domain;

import com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.domain.SectionContext;
import com.pichincha.cdemsaspopenaidocumentation.domain.CodeSignalEvidence;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 * Plain, serialization-friendly request consumed by a {@code SpecialistDocumentationAgent}.
 * Intentionally free of Spring or Temporal types so it can later become the input payload
 * of a Temporal Activity without changing agent business logic.
 */
public record SpecialistAgentRequest(
    String executionId,
    DocumentationGenerationContext generationContext,
    List<CodeSignalEvidence> evidence,
    List<SectionContext> relevantSections) {

  public SpecialistAgentRequest {
    executionId = StringUtils.defaultString(executionId).trim();
    if (generationContext == null) {
      throw new IllegalArgumentException("Generation context is required.");
    }
    evidence = evidence == null ? List.of() : List.copyOf(evidence);
    relevantSections = relevantSections == null ? List.of() : List.copyOf(relevantSections);
  }
}
