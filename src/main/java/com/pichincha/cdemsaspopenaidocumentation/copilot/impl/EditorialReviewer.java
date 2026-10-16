package com.pichincha.cdemsaspopenaidocumentation.copilot.impl;

import com.pichincha.cdemsaspopenaidocumentation.copilot.EditorialReviewAgent;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentContext;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.EditorialReviewRequest;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.RenderedSections;
import com.pichincha.cdemsaspopenaidocumentation.copilot.exception.AgentExecutionException;
import com.pichincha.cdemsaspopenaidocumentation.copilot.support.EditorialRevisionMerger;
import com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.domain.SectionContext;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Runs the editorial review for the templates that enable it. The review only polishes the
 * wording: when it fails, the drafted sections are published and the failure is reported as
 * a warning of the document.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EditorialReviewer {

  private final EditorialReviewAgent editorialReviewAgent;

  public RenderedSections review(DocumentContext context, List<SectionContext> sections,
      RenderedSections drafts) {
    if (!context.template().guidance().editorialReview()
        || drafts.bodiesBySectionId().isEmpty()) {
      return drafts;
    }
    try {
      Map<String, String> revisions = editorialReviewAgent.review(
          new EditorialReviewRequest(context, sections, drafts.bodiesBySectionId()));
      return EditorialRevisionMerger.merge(drafts, revisions);
    } catch (AgentExecutionException exception) {
      log.warn("Editorial review failed for template {}; publishing the drafts.",
          context.template().id(), exception);
      return drafts.withWarning("Editorial review skipped: " + exception.getMessage());
    }
  }
}
