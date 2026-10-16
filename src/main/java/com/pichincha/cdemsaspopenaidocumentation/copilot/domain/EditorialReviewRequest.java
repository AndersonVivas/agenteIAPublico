package com.pichincha.cdemsaspopenaidocumentation.copilot.domain;

import com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.domain.SectionContext;
import java.util.List;
import java.util.Map;

public record EditorialReviewRequest(
    DocumentContext documentContext,
    List<SectionContext> sections,
    Map<String, String> drafts) {

  public EditorialReviewRequest {
    sections = sections == null ? List.of() : List.copyOf(sections);
    drafts = drafts == null ? Map.of() : Map.copyOf(drafts);
  }
}
