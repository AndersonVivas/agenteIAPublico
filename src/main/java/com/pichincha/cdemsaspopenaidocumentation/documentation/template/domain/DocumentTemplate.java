package com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain;

import com.pichincha.cdemsaspopenaidocumentation.documentation.audience.domain.AudienceType;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

public record DocumentTemplate(
    String id,
    String name,
    TemplateVersion version,
    List<TemplateSection> sections,
    TemplateMetadata metadata,
    TemplateGuidance guidance,
    ReaderProfile reader) {

  public DocumentTemplate(String id, String name, TemplateVersion version,
      List<TemplateSection> sections, TemplateMetadata metadata) {
    this(id, name, version, sections, metadata, TemplateGuidance.defaults());
  }

  public DocumentTemplate(String id, String name, TemplateVersion version,
      List<TemplateSection> sections, TemplateMetadata metadata, TemplateGuidance guidance) {
    this(id, name, version, sections, metadata, guidance,
        ReaderProfile.defaultFor(AudienceType.OPERATIONS));
  }

  public DocumentTemplate {
    id = StringUtils.defaultString(id).trim();
    name = StringUtils.defaultString(name).trim();
    version = version == null ? TemplateVersion.initial() : version;
    sections = normalizeSections(sections);
    metadata = metadata == null ? TemplateMetadata.internal() : metadata;
    guidance = guidance == null ? TemplateGuidance.defaults() : guidance;
    reader = reader == null ? ReaderProfile.defaultFor(AudienceType.OPERATIONS) : reader;
  }

  private static List<TemplateSection> normalizeSections(List<TemplateSection> sections) {
    if (sections == null || sections.isEmpty()) {
      return List.of();
    }
    return sections.stream().filter(Objects::nonNull).distinct()
        .sorted(Comparator.comparingInt(TemplateSection::order)
            .thenComparing(TemplateSection::id)).toList();
  }
}
