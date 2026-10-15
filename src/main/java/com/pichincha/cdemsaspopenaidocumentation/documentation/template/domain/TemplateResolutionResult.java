package com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain;

import org.apache.commons.lang3.StringUtils;

public record TemplateResolutionResult(
    DocumentTemplate template,
    TemplateVersion resolvedVersion,
    String resolvedFrom,
    boolean fallbackApplied) {

  public TemplateResolutionResult {
    if (template == null) {
      throw new IllegalArgumentException("Template is required.");
    }
    resolvedVersion = resolvedVersion == null ? template.version() : resolvedVersion;
    resolvedFrom = StringUtils.defaultIfBlank(resolvedFrom, "unknown").trim();
  }

  public static TemplateResolutionResult resolved(DocumentTemplate template,
      String resolvedFrom) {
    return new TemplateResolutionResult(template, template.version(), resolvedFrom, false);
  }

  public static TemplateResolutionResult fallback(DocumentTemplate template,
      String resolvedFrom) {
    return new TemplateResolutionResult(template, template.version(), resolvedFrom, true);
  }
}
