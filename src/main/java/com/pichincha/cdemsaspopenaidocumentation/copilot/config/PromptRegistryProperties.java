package com.pichincha.cdemsaspopenaidocumentation.copilot.config;

import java.nio.file.Path;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Location of the externally stored agent prompts. Prompts are never embedded in source
 * code: they live outside the deployable artifact so their wording can evolve without a
 * recompile or redeploy.
 */
@ConfigurationProperties(prefix = "documentation.prompts")
public record PromptRegistryProperties(String path, String extension) {

  public PromptRegistryProperties {
    path = StringUtils.defaultIfBlank(path, defaultPath()).trim();
    extension = StringUtils.defaultIfBlank(extension, ".md").trim();
  }

  private static String defaultPath() {
    return Path.of(System.getProperty("user.home"), "Documents", "iniciativaIADocumentación",
        "prompts").toString();
  }
}
