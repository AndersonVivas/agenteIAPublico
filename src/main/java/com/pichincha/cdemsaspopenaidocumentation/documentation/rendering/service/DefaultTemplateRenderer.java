package com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.service;

import com.pichincha.cdemsaspopenaidocumentation.documentation.audience.domain.AudienceProfile;
import com.pichincha.cdemsaspopenaidocumentation.documentation.communication.domain.CommunicationProfile;
import com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.TemplateRenderer;
import com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.domain.AdaptedKnowledge;
import com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.domain.RenderingPlan;
import com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.domain.SectionContext;
import com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.domain.SelectedKnowledge;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.DocumentTemplate;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.ProjectKnowledgeBase;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class DefaultTemplateRenderer implements TemplateRenderer {

  private final KnowledgeSelectionEngine selectionEngine;
  private final KnowledgeAdaptationEngine adaptationEngine;
  private final SectionPlanningEngine sectionPlanningEngine;
  private final TraceabilityAssembler traceabilityAssembler;

  public DefaultTemplateRenderer(KnowledgeSelectionEngine selectionEngine,
      KnowledgeAdaptationEngine adaptationEngine,
      SectionPlanningEngine sectionPlanningEngine,
      TraceabilityAssembler traceabilityAssembler) {
    this.selectionEngine = selectionEngine;
    this.adaptationEngine = adaptationEngine;
    this.sectionPlanningEngine = sectionPlanningEngine;
    this.traceabilityAssembler = traceabilityAssembler;
  }

  @Override
  public RenderingPlan render(ProjectKnowledgeBase knowledgeBase,
      DocumentTemplate template, AudienceProfile audienceProfile,
      CommunicationProfile communicationProfile) {
    SelectedKnowledge selected = selectionEngine.select(knowledgeBase, template,
        audienceProfile);
    AdaptedKnowledge adapted = adaptationEngine.adapt(selected, audienceProfile,
        communicationProfile);
    List<SectionContext> sections = sectionPlanningEngine.plan(template, adapted,
        communicationProfile);
    Map<String, List<String>> traceability = traceabilityAssembler.assemble(sections);
    return new RenderingPlan(template.id(), template.name(), template.version(),
        audienceProfile.audienceType(), communicationProfile.profileId(),
        template.name(), selectedIds(selected), selected.excludedFindingIds(), sections,
        traceability, mergeWarnings(selected.warnings(), adapted.warnings()));
  }

  private List<String> selectedIds(SelectedKnowledge selected) {
    return selected.findings().stream().map(finding -> finding.findingId()).toList();
  }

  private List<String> mergeWarnings(List<String> selectionWarnings,
      List<String> adaptationWarnings) {
    List<String> merged = new ArrayList<>(selectionWarnings);
    merged.addAll(adaptationWarnings);
    return merged.stream().distinct().toList();
  }
}
