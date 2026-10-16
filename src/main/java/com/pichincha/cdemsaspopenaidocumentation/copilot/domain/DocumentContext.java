package com.pichincha.cdemsaspopenaidocumentation.copilot.domain;

import com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.domain.RenderingPlan;
import com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.domain.SectionContext;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.DocumentTemplate;
import com.pichincha.cdemsaspopenaidocumentation.domain.enums.DocumentLifecycleEnum;
import java.util.List;
import java.util.Objects;

public record DocumentContext(
    DocumentationGenerationContext generationContext,
    DocumentTemplate template,
    OutputContract outputContract,
    RenderingPlan renderingPlan,
    List<DocumentationSkill> skills,
    String renderSignature) {

  public DocumentContext {
    Objects.requireNonNull(generationContext, "Generation context is required.");
    Objects.requireNonNull(template, "Template is required.");
    Objects.requireNonNull(outputContract, "Output contract is required.");
    Objects.requireNonNull(renderingPlan, "Rendering plan is required.");
    skills = skills == null ? List.of() : List.copyOf(skills);
  }

  public DocumentLifecycleEnum lifecycle() {
    return template.guidance().lifecycle();
  }

  public List<SectionContext> sections() {
    return renderingPlan.sectionContexts();
  }
}
