package com.pichincha.cdemsaspopenaidocumentation.copilot.impl;

import com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.domain.SectionContext;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

/**
 * Deterministically assembles the final Markdown document from the rendering plan's
 * ordered {@code SectionContext} list, combining newly rendered bodies for impacted
 * sections with carried-forward bodies for unchanged sections.
 */
public class DocumentAssembler {

  public String assemble(String documentTitle, List<SectionContext> orderedSections,
      Map<String, String> renderedBodiesBySectionId, Map<String, String> previousBodiesByTitle) {
    StringBuilder builder = new StringBuilder();
    if (StringUtils.isNotBlank(documentTitle)) {
      builder.append("# ").append(documentTitle).append("\n\n");
    }
    for (SectionContext section : orderedSections) {
      builder.append("## ").append(section.title()).append("\n\n");
      builder.append(resolveBody(section, renderedBodiesBySectionId, previousBodiesByTitle))
          .append("\n\n");
    }
    return builder.toString().trim() + "\n";
  }

  private String resolveBody(SectionContext section,
      Map<String, String> renderedBodiesBySectionId, Map<String, String> previousBodiesByTitle) {
    String rendered = renderedBodiesBySectionId.get(section.sectionId());
    if (StringUtils.isNotBlank(rendered)) {
      return rendered;
    }
    String previous = previousBodiesByTitle.get(section.title());
    if (StringUtils.isNotBlank(previous)) {
      return previous;
    }
    return StringUtils.defaultIfBlank(section.emptyStateMessage(),
        "No hay informacion disponible para esta seccion.");
  }
}
