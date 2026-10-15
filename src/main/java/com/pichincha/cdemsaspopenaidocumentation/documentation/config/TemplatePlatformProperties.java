package com.pichincha.cdemsaspopenaidocumentation.documentation.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.apache.commons.lang3.StringUtils;

@ConfigurationProperties(prefix = "documentation.platform")
public record TemplatePlatformProperties(
    DocumentationPlatformMode mode,
    boolean templateResolutionEnabled,
    boolean auditMetadataEnabled,
    boolean dualRegistrationEnabled,
    boolean localSourceEnabled,
    String localSourcePath,
    List<String> localSourceExtensions) {

  public TemplatePlatformProperties {
    mode = mode == null ? DocumentationPlatformMode.LEGACY : mode;
    localSourcePath = StringUtils.defaultString(localSourcePath).trim();
    localSourceExtensions = localSourceExtensions == null
        ? List.of(".yml", ".yaml", ".json", ".md")
        : localSourceExtensions.stream().map(StringUtils::defaultString)
            .map(String::trim).filter(StringUtils::isNotBlank).toList();
  }
}
