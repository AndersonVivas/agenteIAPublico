package com.pichincha.cdemsaspopenaidocumentation.copilot;

import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.TemplateRenderRequest;
import java.util.Map;

/**
 * Produces the final Markdown body for the sections that are actually impacted in this
 * execution, driven entirely by {@code RenderingPlan.sectionContexts()} (title, objective,
 * narrative intent, presentation mode, highlights, traceability and writing instructions).
 * Returns a map of sectionId to rendered Markdown; untouched sections are preserved by the
 * caller from the previous document version. Temporal-Activity-ready: plain request/response.
 */
public interface TemplateRenderAgent {

  Map<String, String> render(TemplateRenderRequest request);
}
