package com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain;

import java.time.Instant;

public record TemplateCatalogEntry(
    DocumentTemplate template,
    TemplateVersion version,
    String publishedFrom,
    TemplateSourceType sourceType,
    Instant registeredAt,
    boolean active,
    boolean fallbackEligible) {

  public TemplateCatalogEntry {
    version = version == null ? template.version() : version;
    publishedFrom = publishedFrom == null ? "unknown" : publishedFrom.trim();
    sourceType = sourceType == null ? TemplateSourceType.LOCAL : sourceType;
    registeredAt = registeredAt == null ? Instant.now() : registeredAt;
  }
}
