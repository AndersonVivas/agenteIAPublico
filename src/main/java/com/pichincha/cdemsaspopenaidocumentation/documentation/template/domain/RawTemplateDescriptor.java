package com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain;

import java.time.Instant;
import org.apache.commons.lang3.StringUtils;

public record RawTemplateDescriptor(
    String sourceId,
    String templateId,
    String templateName,
    TemplateVersion declaredVersion,
    String content,
    TemplateFormat format,
    TemplateSourceType sourceType,
    String sourceReference,
    String checksum,
    Instant lastModifiedAt) {

  public RawTemplateDescriptor {
    sourceId = StringUtils.defaultString(sourceId).trim();
    templateId = StringUtils.defaultString(templateId).trim();
    templateName = StringUtils.defaultString(templateName).trim();
    declaredVersion = declaredVersion == null ? TemplateVersion.initial()
        : declaredVersion;
    content = StringUtils.defaultString(content);
    format = format == null ? TemplateFormat.UNKNOWN : format;
    sourceType = sourceType == null ? TemplateSourceType.LOCAL : sourceType;
    sourceReference = StringUtils.defaultString(sourceReference).trim();
    checksum = StringUtils.defaultString(checksum).trim();
    lastModifiedAt = lastModifiedAt == null ? Instant.EPOCH : lastModifiedAt;
  }
}
