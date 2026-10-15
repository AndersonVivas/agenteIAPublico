package com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pichincha.cdemsaspopenaidocumentation.documentation.audience.domain.AudienceProfile;
import com.pichincha.cdemsaspopenaidocumentation.documentation.audience.domain.AudienceType;
import com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.domain.SelectedKnowledge;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.DocumentTemplate;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateMetadata;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateVersion;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.ApiKnowledge;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.DependencyKnowledge;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.EvidenceRef;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.KnowledgeFinding;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.ProjectKnowledgeBase;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class KnowledgeSelectionEngineTest {

  private final KnowledgeSelectionEngine engine = new KnowledgeSelectionEngine();

  @Test
  void shouldSelectApiAndDependencyFindingsForApiTemplate() {
    SelectedKnowledge selected = engine.select(knowledgeBase(), apiTemplate(),
        AudienceProfile.defaults().get(AudienceType.DEVELOPERS));

    assertEquals(2, selected.findings().size());
    assertTrue(selected.findings().stream()
        .allMatch(f -> f.category().equals("api")
            || f.category().equals("dependency")));
  }

  @Test
  void shouldExcludeFindingsByAudienceConcernFilter() {
    AudienceProfile audience = new AudienceProfile(AudienceType.OPERATIONS,
        "Ops", "es", "ops", "mitigate", List.of(), List.of("framework"),
        "summarized");

    SelectedKnowledge selected = engine.select(knowledgeBase(), genericTemplate(),
        audience);

    assertEquals(2, selected.findings().size());
    assertEquals(1, selected.excludedFindingIds().size());
  }

  private ProjectKnowledgeBase knowledgeBase() {
    KnowledgeFinding api = finding("f-api", "api", "Endpoint timeout");
    KnowledgeFinding dep = finding("f-dep", "dependency", "Library outdated");
    KnowledgeFinding conf = finding("f-config", "configuration",
        "Framework internals leak");
    return new ProjectKnowledgeBase(null, new ApiKnowledge(List.of(api)), null,
        new DependencyKnowledge(List.of(dep)),
        new com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.ConfigurationKnowledge(
            List.of(conf)),
        null, List.of(), List.of(), Map.of());
  }

  private KnowledgeFinding finding(String id, String category, String summary) {
    return new KnowledgeFinding(id, category, id, id, summary, "medium", 0.8,
        List.of(new EvidenceRef(id + "-e", "a.txt", "R1", "signal", 0.9)));
  }

  private DocumentTemplate apiTemplate() {
    return new DocumentTemplate("api-doc", "API Documentation",
        new TemplateVersion("1.0.0"), List.of(), TemplateMetadata.internal());
  }

  private DocumentTemplate genericTemplate() {
    return new DocumentTemplate("ops-doc", "Operations Guide",
        new TemplateVersion("1.0.0"), List.of(), TemplateMetadata.internal());
  }
}
