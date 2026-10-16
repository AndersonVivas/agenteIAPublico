package com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain;

import org.apache.commons.lang3.StringUtils;

public record RawTemplateDescriptor(
    String sourceId,
    String templateId,
    String templateName,
    TemplateVersion declaredVersion,
    String content,
    TemplateFormat format,
    String checksum) {

  public RawTemplateDescriptor {
    sourceId = StringUtils.defaultString(sourceId).trim();
    templateId = StringUtils.defaultString(templateId).trim();
    templateName = StringUtils.defaultString(templateName).trim();
    declaredVersion = declaredVersion == null ? TemplateVersion.initial() : declaredVersion;
    content = StringUtils.defaultString(content);
    format = format == null ? TemplateFormat.UNKNOWN : format;
    checksum = StringUtils.defaultString(checksum).trim();
  }
}
