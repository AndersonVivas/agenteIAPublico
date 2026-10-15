package com.pichincha.cdemsaspopenaidocumentation.copilot.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.pichincha.cdemsaspopenaidocumentation.documentation.audience.domain.AudienceProfile;
import com.pichincha.cdemsaspopenaidocumentation.documentation.audience.domain.AudienceType;
import com.pichincha.cdemsaspopenaidocumentation.documentation.communication.domain.CommunicationProfile;
import com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.domain.RenderingPlan;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.DocumentTemplate;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateMetadata;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateVersion;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.ProjectKnowledgeBase;
import java.util.List;
import org.junit.jupiter.api.Test;

class DocumentContextTest {

  @Test
  void shouldCreateDocumentContextWithValidContracts() {
  DocumentContext context = new DocumentContext("doc-1",
    DocumentTemplateType.TECHNICAL_DOCUMENTATION, generationContext(),
        new ProjectKnowledgeBase(null, null, null, null, null, null,
            List.of(), List.of(), null),
        resolvedTemplateContext(), outputContract(), renderingPlan(),
        new DocumentProvenance("scan-1", "org", "proj", "repo", "/tmp/repo",
            "error-manual", "1.0.0", "catalog", "OPERATIONS",
            "operations-default", 1, 0, 1, 1, null),
        List.of("warning"));

    assertEquals("doc-1", context.documentId());
    assertEquals(1, context.warnings().size());
  }

  @Test
  void shouldFailWhenTemplateTypeIsMissing() {
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> new DocumentContext("doc-1", null, generationContext(),
            new ProjectKnowledgeBase(null, null, null, null, null, null,
                List.of(), List.of(), null),
            resolvedTemplateContext(), outputContract(), renderingPlan(),
            new DocumentProvenance("scan-1", "org", "proj", "repo", "/tmp/repo",
                "error-manual", "1.0.0", "catalog", "OPERATIONS",
                "operations-default", 1, 0, 1, 1, null),
            List.of()));

    assertEquals("Template type is required.", exception.getMessage());
  }

  private DocumentationGenerationContext generationContext() {
    return new DocumentationGenerationContext("scan-1", "repo", "/tmp/repo",
        "org", "proj", 1L, null);
  }

  private ResolvedTemplateContext resolvedTemplateContext() {
    DocumentTemplate template = new DocumentTemplate("error-manual", "Error Manual",
        new TemplateVersion("1.0.0"), List.of(), TemplateMetadata.internal());
    return new ResolvedTemplateContext(template, null, template.version(),
        "catalog", false, AudienceProfile.defaults().get(AudienceType.OPERATIONS),
        CommunicationProfile.defaultProfile(), List.of());
  }

  private OutputContract outputContract() {
    return new OutputContract("markdown", "es", "Objective",
        "No evidenciado en el escaneo.", true, List.of("## Section"),
        List.of("symptom"), List.of("Use only evidence"),
        List.of("Never invent information."));
  }

  private RenderingPlan renderingPlan() {
    return new RenderingPlan("error-manual", "Error Manual",
        new TemplateVersion("1.0.0"), AudienceType.OPERATIONS,
        "operations-default", "Objective", List.of("f1"), List.of(),
        List.of(), java.util.Map.of(), List.of());
  }
}
