package com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pichincha.cdemsaspopenaidocumentation.documentation.audience.domain.AudienceProfile;
import com.pichincha.cdemsaspopenaidocumentation.documentation.audience.domain.AudienceType;
import com.pichincha.cdemsaspopenaidocumentation.documentation.communication.domain.CommunicationProfile;
import com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.domain.RenderingPlan;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.DocumentTemplate;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateMetadata;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateSection;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateVersion;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.ApiKnowledge;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.EvidenceRef;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.KnowledgeFinding;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.ProjectKnowledgeBase;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class DefaultTemplateRendererTest {

  private final DefaultTemplateRenderer renderer = new DefaultTemplateRenderer(
      new KnowledgeSelectionEngine(), new KnowledgeAdaptationEngine(),
      new SectionPlanningEngine(), new TraceabilityAssembler());

  @Test
  void shouldRenderPlanWithSectionsAndTraceability() {
    RenderingPlan plan = renderer.render(knowledgeBase(), template(),
        AudienceProfile.defaults().get(AudienceType.OPERATIONS),
        CommunicationProfile.defaultProfile());

    assertEquals("api-doc", plan.templateId());
    assertEquals(1, plan.sectionContexts().size());
    assertEquals(1, plan.selectedFindingIds().size());
    assertTrue(plan.traceabilityIndex().containsKey("api"));
  }

  private ProjectKnowledgeBase knowledgeBase() {
    KnowledgeFinding finding = new KnowledgeFinding("f1", "api", "k1", "title",
        "Timeout in upstream call", "high", 0.9,
        List.of(new EvidenceRef("e1", "a.txt", "R", "signal", 0.9)));
    return new ProjectKnowledgeBase(null, new ApiKnowledge(List.of(finding)), null,
        null, null, null, List.of(), List.of(), Map.of());
  }

  private DocumentTemplate template() {
    TemplateSection section = new TemplateSection("api", "API Summary",
        "Describe API findings", List.of(), true, 1);
    return new DocumentTemplate("api-doc", "API Documentation",
        new TemplateVersion("1.0.0"), List.of(section),
        TemplateMetadata.internal());
  }
}
