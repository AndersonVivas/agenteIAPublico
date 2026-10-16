package com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.service;

import com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.TemplateRenderer;
import com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.domain.RenderingPlan;
import com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.domain.SelectedKnowledge;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.DocumentTemplate;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.KnowledgeConflict;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.KnowledgeGap;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.ProjectKnowledgeBase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DefaultTemplateRenderer implements TemplateRenderer {

  private final KnowledgeSelectionEngine selectionEngine;
  private final SectionPlanningEngine sectionPlanningEngine;

  @Override
  public RenderingPlan render(ProjectKnowledgeBase knowledgeBase, DocumentTemplate template) {
    SelectedKnowledge selected = selectionEngine.select(knowledgeBase, template);
    return new RenderingPlan(sectionPlanningEngine.plan(template, selected),
        selected.gaps().stream().map(KnowledgeGap::description).toList(),
        selected.conflicts().stream().map(KnowledgeConflict::description).toList(),
        selected.warnings());
  }
}
