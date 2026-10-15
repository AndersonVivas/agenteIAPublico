package com.pichincha.cdemsaspopenaidocumentation.documentation.config;

import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "documentation.templates")
public record DocumentationTemplatesProperties(
    String path,
    List<String> extensions) {

  public DocumentationTemplatesProperties {
    path = StringUtils.defaultString(path).trim();
    extensions = extensions == null ? List.of(".yml", ".yaml", ".json", ".md")
        : extensions.stream().map(StringUtils::defaultString).map(String::trim)
            .filter(StringUtils::isNotBlank).toList();
  }

  public boolean enabled() {
    return StringUtils.isNotBlank(path);
  }
}