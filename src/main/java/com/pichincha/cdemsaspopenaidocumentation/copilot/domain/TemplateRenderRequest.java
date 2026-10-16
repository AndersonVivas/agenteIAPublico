package com.pichincha.cdemsaspopenaidocumentation.copilot.domain;

import com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.domain.SectionContext;
import java.util.List;
import java.util.Objects;

public record TemplateRenderRequest(
    DocumentContext documentContext,
    List<SectionContext> impactedSections,
    ConsolidatedKnowledge consolidatedKnowledge) {

  public TemplateRenderRequest {
    Objects.requireNonNull(documentContext, "Document context is required.");
    Objects.requireNonNull(consolidatedKnowledge, "Consolidated knowledge is required.");
    impactedSections = impactedSections == null ? List.of() : List.copyOf(impactedSections);
  }
}
