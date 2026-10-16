package com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain;

import com.pichincha.cdemsaspopenaidocumentation.domain.enums.TemplateStatusEnum;
import org.apache.commons.lang3.StringUtils;

public record TemplateMetadata(String checksum, TemplateStatusEnum status) {

  public TemplateMetadata {
    checksum = StringUtils.defaultString(checksum).trim();
    status = status == null ? TemplateStatusEnum.ACTIVE : status;
  }

  public static TemplateMetadata internal() {
    return new TemplateMetadata("", TemplateStatusEnum.ACTIVE);
  }

  public boolean active() {
    return status == TemplateStatusEnum.ACTIVE;
  }
}
