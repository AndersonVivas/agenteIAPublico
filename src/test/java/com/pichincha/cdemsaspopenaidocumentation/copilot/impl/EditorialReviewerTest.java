package com.pichincha.cdemsaspopenaidocumentation.copilot.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pichincha.cdemsaspopenaidocumentation.copilot.EditorialReviewAgent;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentContext;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.EditorialReviewRequest;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.OutputContract;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.RenderedSections;
import com.pichincha.cdemsaspopenaidocumentation.copilot.exception.AgentExecutionException;
import com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.domain.RenderingPlan;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.DocumentTemplate;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateGuidance;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateMetadata;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateVersion;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class EditorialReviewerTest {

  private final RenderedSections drafts = new RenderedSections(Map.of("summary", "Draft."),
      List.of());

  @Test
  void reviewShouldReturnTheDraftsWithoutCallingTheAgentWhenTheReviewIsDisabled() {
    CountingAgent agent = new CountingAgent(false);

    RenderedSections result = new EditorialReviewer(agent).review(context(false), List.of(),
        drafts);

    assertEquals(drafts, result);
    assertEquals(0, agent.invocations);
  }

  @Test
  void reviewShouldApplyTheRevisionsWhenTheReviewIsEnabled() {
    RenderedSections result = new EditorialReviewer(new CountingAgent(false))
        .review(context(true), List.of(), drafts);

    assertEquals("Revised.", result.bodyOf("summary"));
  }

  @Test
  void reviewShouldKeepTheDraftsAndWarnWhenTheAgentFails() {
    RenderedSections result = new EditorialReviewer(new CountingAgent(true))
        .review(context(true), List.of(), drafts);

    assertEquals("Draft.", result.bodyOf("summary"));
    assertTrue(result.warnings().getFirst().startsWith("Editorial review skipped"));
  }

  private DocumentContext context(boolean editorialReview) {
    DocumentTemplate template = new DocumentTemplate("error-manual", "Error Manual",
        new TemplateVersion("1.0.0"), List.of(), TemplateMetadata.internal(),
        new TemplateGuidance(null, List.of(), null, List.of(), editorialReview));
    return new DocumentContext(TestDocumentationContextFactory.context(), template,
        new OutputContract("es", "Sin datos.", List.of()),
        new RenderingPlan(List.of(), List.of(), List.of(), List.of()), List.of(), "sig");
  }

  private static final class CountingAgent implements EditorialReviewAgent {

    private final boolean failing;
    private int invocations;

    private CountingAgent(boolean failing) {
      this.failing = failing;
    }

    @Override
    public Map<String, String> review(EditorialReviewRequest request) {
      invocations++;
      if (failing) {
        throw new AgentExecutionException("editorial-review", "Copilot CLI call failed");
      }
      return Map.of("summary", "Revised.");
    }

    @Override
    public String reviewSignature() {
      return "editor";
    }
  }
}
