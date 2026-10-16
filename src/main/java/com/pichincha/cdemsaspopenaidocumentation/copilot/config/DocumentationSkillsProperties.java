package com.pichincha.cdemsaspopenaidocumentation.copilot.config;

import java.nio.file.Path;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "documentation.skills")
public record DocumentationSkillsProperties(Path path) {

  public DocumentationSkillsProperties {
    path = path == null ? defaultPath() : path;
  }

  private static Path defaultPath() {
    return Path.of(System.getProperty("user.home"), "Documents", "iniciativaIADocumentación",
        "skills");
  }
}
