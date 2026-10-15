package com.pichincha.cdemsaspopenaidocumentation.copilot.domain;

import com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.domain.SectionContext;
import java.util.List;

public record TemplateRenderRequest(
    DocumentationGenerationContext generationContext,
    ResolvedTemplateContext resolvedTemplateContext,
    List<SectionContext> impactedSections,
    ConsolidatedKnowledge consolidatedKnowledge,
    OutputContract outputContract) {

  public TemplateRenderRequest {
    if (generationContext == null) {
      throw new IllegalArgumentException("Generation context is required.");
    }
    if (resolvedTemplateContext == null) {
      throw new IllegalArgumentException("Resolved template context is required.");
    }
    if (consolidatedKnowledge == null) {
      throw new IllegalArgumentException("Consolidated knowledge is required.");
    }
    if (outputContract == null) {
      throw new IllegalArgumentException("Output contract is required.");
    }
    impactedSections = impactedSections == null ? List.of() : List.copyOf(impactedSections);
  }
}
