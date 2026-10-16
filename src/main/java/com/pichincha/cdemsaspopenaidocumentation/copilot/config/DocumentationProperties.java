package com.pichincha.cdemsaspopenaidocumentation.copilot.config;

import java.nio.file.Path;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "documentation")
public record DocumentationProperties(Path outputRoot) {

  public DocumentationProperties {
    outputRoot = outputRoot == null ? defaultOutputRoot() : outputRoot;
  }

  private static Path defaultOutputRoot() {
    return Path.of(System.getProperty("user.home"), "Documents", "iniciativaIADocumentación",
        "documentación");
  }
}
