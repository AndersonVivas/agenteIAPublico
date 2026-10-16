package com.pichincha.cdemsaspopenaidocumentation.copilot;

import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.TemplateRenderRequest;
import java.util.Map;

/**
 * Produces the final Markdown body for the sections that are actually impacted in this
 * execution, driven entirely by {@code RenderingPlan.sectionContexts()} (title, objective,
 * narrative intent, presentation mode, highlights, traceability and writing instructions).
 * Returns a map of sectionId to rendered Markdown; untouched sections are preserved by the
 * caller from the previous document version. The render signature fingerprints the prompt and
 * model, so changing either of them invalidates the sections rendered before.
 */
public interface TemplateRenderAgent {

  Map<String, String> render(TemplateRenderRequest request);

  String renderSignature();
}
