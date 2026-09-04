package com.pichincha.cdemsaspopenaidocumentation.prompt.domain;

import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public record RepositoryMetadata(
    String repositoryName,
    String repositoryPath,
    String architecture,
    String primaryTechnology,
    Map<String, String> attributes) {

  public RepositoryMetadata {
    repositoryName = normalize(repositoryName);
    repositoryPath = normalize(repositoryPath);
    architecture = normalize(architecture);
    primaryTechnology = normalize(primaryTechnology);
    attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
  }

  private static String normalize(String value) {
    return StringUtils.defaultString(value).trim();
  }
}