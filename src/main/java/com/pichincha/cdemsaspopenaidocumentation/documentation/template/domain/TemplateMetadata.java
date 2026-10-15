package com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public record TemplateMetadata(
    String source,
    String sourceReference,
    Instant publishedAt,
    List<String> tags,
    Map<String, String> attributes) {

  public TemplateMetadata {
    source = StringUtils.defaultIfBlank(source, "internal").trim();
    sourceReference = StringUtils.defaultString(sourceReference).trim();
    publishedAt = publishedAt == null ? Instant.EPOCH : publishedAt;
    tags = tags == null ? List.of() : tags.stream().map(StringUtils::defaultString)
        .map(String::trim).filter(StringUtils::isNotBlank).toList();
    attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
  }

  public static TemplateMetadata internal() {
    return new TemplateMetadata("internal", "", Instant.EPOCH, List.of(), Map.of());
  }
}
