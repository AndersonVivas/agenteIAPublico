package com.pichincha.cdemsaspopenaidocumentation.config;

import java.nio.file.Path;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.git")
public record GitCloneProperties(
    String baseUrl,
    String username,
    String personalAccessToken,
    Path cloneDirectory,
    boolean overwriteExisting) {

  public GitCloneProperties {
    baseUrl = normalizeBaseUrl(baseUrl);
    cloneDirectory = cloneDirectory == null ? defaultCloneDirectory() : cloneDirectory;
  }

  public String repositoryUrl(String organization, String projectName,
      String repositoryName) {
    if (StringUtils.isBlank(baseUrl)) {
      throw new IllegalStateException("app.git.base-url must be configured.");
    }
    return "https://" + credentialPrefix() + baseUrl + sanitizeSegment(organization) + "/"
        + sanitizeSegment(projectName) + "/_git/"
        + sanitizeSegment(repositoryName);
  }

  private String credentialPrefix() {
    if (StringUtils.isAnyBlank(username, personalAccessToken)) {
      return "";
    }
    return username.trim() + ":" + personalAccessToken.trim() + "@";
  }

  public Path repositoryDirectory(String organization, String projectName,
      String repositoryName) {
    return cloneDirectory
        .resolve(sanitizeSegment(organization))
        .resolve(sanitizeSegment(projectName))
        .resolve(sanitizeSegment(repositoryName));
  }

  public String sanitizeSegment(String value) {
    return value.trim().replaceAll("[^a-zA-Z0-9._-]", "-");
  }

  private static String normalizeBaseUrl(String value) {
    if (StringUtils.isBlank(value)) {
      return "";
    }
    String trimmedValue = value.trim();
    if (trimmedValue.startsWith("https://")) {
      trimmedValue = trimmedValue.substring("https://".length());
    }
    if (trimmedValue.startsWith("http://")) {
      trimmedValue = trimmedValue.substring("http://".length());
    }
    return trimmedValue.endsWith("/") ? trimmedValue : trimmedValue + "/";
  }

  private static Path defaultCloneDirectory() {
    return Path.of(System.getProperty("user.home"), "Documents",
        "repositorios de agentes");
  }
}