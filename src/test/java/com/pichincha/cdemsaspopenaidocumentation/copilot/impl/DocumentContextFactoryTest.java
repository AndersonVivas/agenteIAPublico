package com.pichincha.cdemsaspopenaidocumentation.copilot.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pichincha.cdemsaspopenaidocumentation.copilot.DocumentationSkillRepository;
import com.pichincha.cdemsaspopenaidocumentation.copilot.EditorialReviewAgent;
import com.pichincha.cdemsaspopenaidocumentation.copilot.TemplateRenderAgent;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentContext;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentationSkill;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.EditorialReviewRequest;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.TemplateRenderRequest;
import com.pichincha.cdemsaspopenaidocumentation.documentation.audience.domain.AudienceType;
import com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.TemplateRenderer;
import com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.domain.RenderingPlan;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.DocumentTemplate;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.ReaderProfile;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateGuidance;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateMetadata;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateVersion;
import com.pichincha.cdemsaspopenaidocumentation.domain.enums.DocumentLifecycleEnum;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.ProjectKnowledgeBase;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class DocumentContextFactoryTest {

  private final CapturingRenderer renderer = new CapturingRenderer();
  private final InMemorySkillRepository skillRepository = new InMemorySkillRepository();
  private final FixedSignatureEditorialAgent editorialAgent = new FixedSignatureEditorialAgent();
  private final DocumentContextFactory factory = new DocumentContextFactory(renderer,
      new FixedSignatureRenderAgent(), editorialAgent, skillRepository);

  @Test
  void createShouldBuildRenderingPlanForTheTemplate() {
    DocumentContext context = create(template("checksum-1", false));

    assertTrue(renderer.called);
    assertEquals("error-manual", context.template().id());
    assertEquals(DocumentLifecycleEnum.HISTORICAL, context.lifecycle());
  }

  @Test
  void createShouldTakeOutputContractFromTemplateGuidanceAndReader() {
    DocumentContext context = create(template("checksum-1", false));

    assertEquals("es", context.outputContract().outputLanguage());
    assertEquals("Sin datos.", context.outputContract().missingDataMessage());
    assertEquals(List.of("Never invent information."), context.outputContract().rules());
  }

  @Test
  void createShouldUseTheLanguageOfTheTemplateReader() {
    DocumentTemplate template = new DocumentTemplate("api-guide", "API Guide",
        new TemplateVersion("1.0.0"), List.of(), TemplateMetadata.internal(),
        TemplateGuidance.defaults(), ReaderProfile.defaultFor(AudienceType.DEVELOPERS));

    assertEquals("en", create(template).outputContract().outputLanguage());
  }

  @Test
  void createShouldChangeRenderSignatureWhenTemplateContentChanges() {
    DocumentContext first = create(template("checksum-1", false));
    DocumentContext second = create(template("checksum-2", false));

    assertNotEquals(first.renderSignature(), second.renderSignature());
  }

  @Test
  void createShouldLoadTemplateSkills() {
    DocumentContext context = create(template("checksum-1", false));

    assertEquals(List.of("lenguaje-claro"), skillRepository.requestedNames);
    assertEquals("lenguaje-claro", context.skills().getFirst().name());
  }

  @Test
  void createShouldChangeRenderSignatureWhenSkillInstructionsChange() {
    DocumentContext first = create(template("checksum-1", false));
    skillRepository.instructions = "Use short sentences and explain acronyms.";
    DocumentContext second = create(template("checksum-1", false));

    assertNotEquals(first.renderSignature(), second.renderSignature());
  }

  @Test
  void createShouldIncludeTheEditorialSignatureOnlyWhenTheReviewIsEnabled() {
    String withoutReview = create(template("checksum-1", false)).renderSignature();
    String withReview = create(template("checksum-1", true)).renderSignature();
    editorialAgent.signature = "editor-v2";

    assertEquals(withoutReview, create(template("checksum-1", false)).renderSignature());
    assertNotEquals(withReview, create(template("checksum-1", true)).renderSignature());
  }

  private DocumentContext create(DocumentTemplate template) {
    return factory.create(TestDocumentationContextFactory.context(),
        new ProjectKnowledgeBase(null, null, null, null, null, null, List.of(), List.of()),
        template);
  }

  private DocumentTemplate template(String checksum, boolean editorialReview) {
    TemplateGuidance guidance = new TemplateGuidance(DocumentLifecycleEnum.HISTORICAL,
        List.of("Never invent information."), "Sin datos.", List.of("lenguaje-claro"),
        editorialReview);
    return new DocumentTemplate("error-manual", "Error Manual", new TemplateVersion("1.0.0"),
        List.of(), new TemplateMetadata(checksum, null), guidance);
  }

  private static final class CapturingRenderer implements TemplateRenderer {

    private boolean called;

    @Override
    public RenderingPlan render(ProjectKnowledgeBase knowledgeBase, DocumentTemplate template) {
      called = true;
      return new RenderingPlan(List.of(), List.of(), List.of(), List.of());
    }
  }

  private static final class InMemorySkillRepository implements DocumentationSkillRepository {

    private List<String> requestedNames;
    private String instructions = "Use short sentences.";

    @Override
    public List<DocumentationSkill> findAll(List<String> skillNames) {
      requestedNames = skillNames;
      return skillNames.stream()
          .map(name -> new DocumentationSkill(name, "Plain language", instructions))
          .toList();
    }
  }

  private static final class FixedSignatureRenderAgent implements TemplateRenderAgent {

    @Override
    public Map<String, String> render(TemplateRenderRequest request) {
      return Map.of();
    }

    @Override
    public String renderSignature() {
      return "render-signature";
    }
  }

  private static final class FixedSignatureEditorialAgent implements EditorialReviewAgent {

    private String signature = "editor-v1";

    @Override
    public Map<String, String> review(EditorialReviewRequest request) {
      return Map.of();
    }

    @Override
    public String reviewSignature() {
      return signature;
    }
  }
}
