package com.pichincha.cdemsaspopenaidocumentation.copilot.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "documentation")
public record DocumentationProperties(
    String outputDirectory,
    boolean overwriteExisting,
    boolean evolutionaryMode) {

  public DocumentationProperties {
    outputDirectory = StringUtils.defaultIfBlank(outputDirectory, "docs");
  }
}
