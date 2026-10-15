package com.pichincha.cdemsaspopenaidocumentation.copilot.domain;

import com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.domain.RenderingPlan;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.ProjectKnowledgeBase;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

public record DocumentContext(
    String documentId,
    DocumentTemplateType templateType,
    DocumentationGenerationContext generationContext,
    ProjectKnowledgeBase knowledgeBase,
    ResolvedTemplateContext resolvedTemplateContext,
    OutputContract outputContract,
    RenderingPlan renderingPlan,
    DocumentProvenance documentProvenance,
    List<String> warnings) {

  public DocumentContext {
    documentId = StringUtils.defaultString(documentId).trim();
    if (templateType == null) {
      throw new IllegalArgumentException("Template type is required.");
    }
    if (generationContext == null) {
      throw new IllegalArgumentException("Generation context is required.");
    }
    if (knowledgeBase == null) {
      throw new IllegalArgumentException("Knowledge base is required.");
    }
    if (resolvedTemplateContext == null) {
      throw new IllegalArgumentException("Resolved template context is required.");
    }
    if (outputContract == null) {
      throw new IllegalArgumentException("Output contract is required.");
    }
    if (renderingPlan == null) {
      throw new IllegalArgumentException("Rendering plan is required.");
    }
    if (documentProvenance == null) {
      throw new IllegalArgumentException("Document provenance is required.");
    }
    warnings = warnings == null ? List.of() : List.copyOf(warnings);
  }
}
