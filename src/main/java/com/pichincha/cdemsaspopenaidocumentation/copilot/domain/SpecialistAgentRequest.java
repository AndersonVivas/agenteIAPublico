package com.pichincha.cdemsaspopenaidocumentation.copilot.domain;

import com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.domain.SectionContext;
import com.pichincha.cdemsaspopenaidocumentation.domain.CodeSignalEvidence;
import java.util.List;
import java.util.Objects;

public record SpecialistAgentRequest(
    DocumentationGenerationContext generationContext,
    List<CodeSignalEvidence> evidence,
    List<SectionContext> relevantSections) {

  public SpecialistAgentRequest {
    Objects.requireNonNull(generationContext, "Generation context is required.");
    evidence = evidence == null ? List.of() : List.copyOf(evidence);
    relevantSections = relevantSections == null ? List.of() : List.copyOf(relevantSections);
  }
}
