package com.pichincha.cdemsaspopenaidocumentation.domain;

import java.util.List;
import org.apache.commons.lang3.StringUtils;

public record RepositoryCoordinates(
    String organization,
    String projectName,
    String repositoryName) {

  private static final String UNSAFE_CHARACTERS = "[^a-zA-Z0-9._-]";
  private static final String DOTS_ONLY = "^\\.+$";
  private static final String SAFE_PLACEHOLDER = "_";

  public RepositoryCoordinates {
    organization = StringUtils.defaultString(organization).trim();
    projectName = StringUtils.defaultString(projectName).trim();
    repositoryName = StringUtils.defaultString(repositoryName).trim();
  }

  public String key() {
    return String.join("/", pathSegments());
  }

  public List<String> pathSegments() {
    return List.of(safeSegment(organization), safeSegment(projectName),
        safeSegment(repositoryName));
  }

  public static String safeSegment(String value) {
    String sanitized = StringUtils.defaultString(value).trim()
        .replaceAll(UNSAFE_CHARACTERS, "-");
    if (sanitized.isBlank()) {
      return SAFE_PLACEHOLDER;
    }
    return sanitized.replaceAll(DOTS_ONLY, SAFE_PLACEHOLDER);
  }
}
