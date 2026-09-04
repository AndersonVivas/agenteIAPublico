package com.pichincha.cdemsaspopenaidocumentation.prompt.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.prompts")
public record PromptBuilderProperties(String templatesPath, String rulesPath) {

  public PromptBuilderProperties {
    templatesPath = StringUtils.defaultIfBlank(templatesPath, "prompts/templates");
    rulesPath = StringUtils.defaultIfBlank(rulesPath, "prompts/rules");
  }
}