package com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.pichincha.cdemsaspopenaidocumentation.documentation.communication.domain.CommunicationProfile;
import com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.domain.AdaptedFinding;
import com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.domain.AdaptedKnowledge;
import com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.domain.SectionContext;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.DocumentTemplate;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateMetadata;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateSection;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateVersion;
import java.util.List;
import org.junit.jupiter.api.Test;

class SectionPlanningEngineTest {

  private final SectionPlanningEngine engine = new SectionPlanningEngine();

  @Test
  void shouldGenerateWarningForRequiredSectionWithoutFindings() {
    DocumentTemplate template = new DocumentTemplate("id", "Ops",
        new TemplateVersion("1.0.0"), List.of(section()),
        TemplateMetadata.internal());
    AdaptedKnowledge adapted = new AdaptedKnowledge(List.of(), List.of(), List.of(),
        List.of());

    List<SectionContext> sections = engine.plan(template, adapted,
        CommunicationProfile.defaultProfile());

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
    AdaptedKnowledge adapted = new AdaptedKnowledge(List.of(apiFinding()),
        List.of(), List.of(), List.of());

    List<SectionContext> sections = engine.plan(template, adapted,
        CommunicationProfile.defaultProfile());

    assertEquals(1, sections.get(0).highlights().size());
    assertEquals("", sections.get(0).emptyStateMessage());
  }

  private TemplateSection section() {
    return new TemplateSection("api", "API Summary", "API status", List.of(), true,
        1);
  }

  private AdaptedFinding apiFinding() {
    return new AdaptedFinding("f1", "api", "high", "t", "summary", List.of());
  }
}
