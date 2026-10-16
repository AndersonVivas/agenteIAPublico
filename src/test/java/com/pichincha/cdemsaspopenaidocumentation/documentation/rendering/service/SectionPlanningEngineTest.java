package com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.domain.SectionContext;
import com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.domain.SelectedKnowledge;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.DocumentTemplate;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateMetadata;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateSection;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateVersion;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.KnowledgeFinding;
import java.util.List;
import org.junit.jupiter.api.Test;

class SectionPlanningEngineTest {

  private final SectionPlanningEngine engine = new SectionPlanningEngine();

  @Test
  void shouldGenerateWarningForRequiredSectionWithoutFindings() {
    DocumentTemplate template = new DocumentTemplate("id", "Ops",
        new TemplateVersion("1.0.0"), List.of(section()),
        TemplateMetadata.internal());
    SelectedKnowledge selected = new SelectedKnowledge(List.of(), List.of(), List.of(),
        List.of());

    List<SectionContext> sections = engine.plan(template, selected);

    assertEquals(1, sections.size());
    assertEquals("No evidenciado en el escaneo.",
        sections.get(0).emptyStateMessage());
    assertEquals(1, sections.get(0).warnings().size());
  }

  @Test
  void shouldMapApiFindingsToApiSection() {
    DocumentTemplate template = new DocumentTemplate("id", "Ops",
        new TemplateVersion("1.0.0"), List.of(section()),
        TemplateMetadata.internal());
    SelectedKnowledge selected = new SelectedKnowledge(List.of(apiFinding()),
        List.of(), List.of(), List.of());

    List<SectionContext> sections = engine.plan(template, selected);

    assertEquals(1, sections.get(0).highlights().size());
    assertEquals("", sections.get(0).emptyStateMessage());
  }

  private TemplateSection section() {
    return new TemplateSection("api", "API Summary", "API status", List.of(), true,
        1);
  }

  private KnowledgeFinding apiFinding() {
    return new KnowledgeFinding("f1", "api", "key", "title", "summary", "high", 0.9,
        List.of());
  }
}
