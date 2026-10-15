package com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain;

import org.apache.commons.lang3.StringUtils;

public record TemplateVersion(String value) {

  public TemplateVersion {
    value = StringUtils.defaultIfBlank(value, "0.0.0").trim();
  }

  public static TemplateVersion initial() {
    return new TemplateVersion("0.0.0");
  }
}
