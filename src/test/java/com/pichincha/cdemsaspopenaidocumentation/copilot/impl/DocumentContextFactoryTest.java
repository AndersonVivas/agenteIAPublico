package com.pichincha.cdemsaspopenaidocumentation.copilot.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentContext;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentTemplateType;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentationGenerationContext;
import com.pichincha.cdemsaspopenaidocumentation.documentation.audience.domain.AudienceProfile;
import com.pichincha.cdemsaspopenaidocumentation.documentation.audience.domain.AudienceType;
import com.pichincha.cdemsaspopenaidocumentation.documentation.audience.service.AudienceProfileResolver;
import com.pichincha.cdemsaspopenaidocumentation.documentation.communication.domain.CommunicationProfile;
import com.pichincha.cdemsaspopenaidocumentation.documentation.communication.service.CommunicationProfileResolver;
import com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.TemplateRenderer;
import com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.domain.RenderingPlan;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.DocumentTemplate;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateMetadata;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateResolutionResult;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateVersion;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.port.TemplateRepository;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.ProjectKnowledgeBase;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class DocumentContextFactoryTest {

  @Test
  void shouldBuildDocumentContextAndUseTemplateRenderer() {
    CapturingRenderer renderer = new CapturingRenderer();
    DocumentContextFactory factory = new DocumentContextFactory(repository(),
        new AudienceProfileResolver(), new CommunicationProfileResolver(), renderer);

    DocumentContext context = factory.create(generationContext(), knowledgeBase(),
      "error-manual");

    assertEquals(DocumentTemplateType.TECHNICAL_DOCUMENTATION,
      context.templateType());
    assertEquals("error-manual", context.resolvedTemplateContext().template().id());
    assertEquals(AudienceType.OPERATIONS,
        context.resolvedTemplateContext().audienceProfile().audienceType());
    assertTrue(renderer.called);
    assertEquals("api-doc", context.renderingPlan().templateId());
  }

  @Test
  void shouldResolveTechnicalAudienceForDevelopers() {
    CapturingRenderer renderer = new CapturingRenderer();
    DocumentContextFactory factory = new DocumentContextFactory(repository(),
        new AudienceProfileResolver(), new CommunicationProfileResolver(), renderer);

    DocumentContext context = factory.create(generationContext(), knowledgeBase(),
      "error-manual", AudienceType.DEVELOPERS);

    assertEquals(AudienceType.DEVELOPERS,
        context.resolvedTemplateContext().audienceProfile().audienceType());
    assertEquals("en", context.outputContract().outputLanguage());
  }

  @Test
  void shouldResolveFunctionalAudience() {
    CapturingRenderer renderer = new CapturingRenderer();
    DocumentContextFactory factory = new DocumentContextFactory(repository(),
        new AudienceProfileResolver(), new CommunicationProfileResolver(), renderer);

    DocumentContext context = factory.create(generationContext(), knowledgeBase(),
      "error-manual", AudienceType.FUNCTIONAL_USERS);

    assertEquals(AudienceType.FUNCTIONAL_USERS,
        context.resolvedTemplateContext().audienceProfile().audienceType());
    assertTrue(context.outputContract().objective().contains("verified knowledge"));
  }

  private TemplateRepository repository() {
    DocumentTemplate template = new DocumentTemplate("error-manual", "Error Manual",
        new TemplateVersion("1.0.0"), List.of(), TemplateMetadata.internal());
    return new TemplateRepository() {
      @Override
      public Optional<TemplateResolutionResult> resolveActive(String templateId) {
        return Optional.of(TemplateResolutionResult.resolved(template, "catalog"));
      }

      @Override
      public Optional<TemplateResolutionResult> resolveVersion(String templateId,
          TemplateVersion version) {
        return Optional.empty();
      }

      @Override
      public List<TemplateResolutionResult> searchByName(String templateName) {
        return List.of();
      }
    };
  }

  private DocumentationGenerationContext generationContext() {
    return new DocumentationGenerationContext("scan-1", "repo", "/tmp/repo",
        "org", "proj", 1L, null);
  }

  private ProjectKnowledgeBase knowledgeBase() {
    return new ProjectKnowledgeBase(null, null, null, null, null, null,
        List.of(), List.of(), null);
  }

  private static class CapturingRenderer implements TemplateRenderer {

    private boolean called;

    @Override
    public RenderingPlan render(ProjectKnowledgeBase knowledgeBase,
        DocumentTemplate template, AudienceProfile audienceProfile,
        CommunicationProfile communicationProfile) {
      called = true;
      return new RenderingPlan("api-doc", "Api", new TemplateVersion("1.0.0"),
          AudienceType.OPERATIONS, "operations-default", "x", List.of(),
          List.of(), List.of(), java.util.Map.of(), List.of());
    }
  }
}
