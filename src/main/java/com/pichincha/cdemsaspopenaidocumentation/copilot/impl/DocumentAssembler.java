package com.pichincha.cdemsaspopenaidocumentation.copilot.impl;

import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentHeader;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentStateSnapshot;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.RenderedSections;
import com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.domain.SectionContext;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * Deterministically assembles the final Markdown document: a metadata header, then every
 * section of the rendering plan in order, taking the newly rendered body when the section was
 * regenerated and the stored body of the previous version otherwise.
 */
@Component
public class DocumentAssembler {

  private static final String DEFAULT_EMPTY_BODY =
      "No hay informacion disponible para esta seccion.";

  public String assemble(DocumentHeader header, List<SectionContext> orderedSections,
      RenderedSections rendered, DocumentStateSnapshot previousState) {
    StringBuilder builder = new StringBuilder(frontMatter(header))
        .append("# ").append(header.title()).append("\n\n");
    for (SectionContext section : orderedSections) {
      builder.append("## ").append(section.title()).append("\n\n")
          .append(resolveBody(section, rendered, previousState)).append("\n\n");
    }
    return builder.toString().trim() + "\n";
  }

  private String resolveBody(SectionContext section, RenderedSections rendered,
      DocumentStateSnapshot previousState) {
    if (rendered.contains(section.sectionId())) {
      return rendered.bodyOf(section.sectionId());
    }
    return previousState.bodyOf(section.sectionId())
        .filter(StringUtils::isNotBlank)
        .orElseGet(() -> StringUtils.defaultIfBlank(section.emptyStateMessage(),
            DEFAULT_EMPTY_BODY));
  }

  private String frontMatter(DocumentHeader header) {
    return String.join("\n", "---",
        "template: " + header.templateId(),
        "template-version: " + header.templateVersion(),
        "lifecycle: " + header.lifecycle(),
        "repository: " + header.repositoryKey(),
        "document-version: " + header.documentVersion(),
        "scan-id: " + header.scanId(),
        "commit: " + header.headCommit(),
        "pull-request: " + header.pullRequestId(),
        "generated-at: " + header.generatedAt(),
        "---", "", "");
  }
}
