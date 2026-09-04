package com.pichincha.cdemsaspopenaidocumentation.copilot.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public record PromptExecutionRequest(
    @NotBlank String promptId,
    @NotBlank String repositoryName,
    @NotBlank String repositoryPath,
    @NotNull DocumentTemplateType templateType,
    @NotBlank String promptContent,
    Map<String, String> metadata) {

  public PromptExecutionRequest {
    promptId = normalize(promptId);
    repositoryName = normalize(repositoryName);
    repositoryPath = normalize(repositoryPath);
    promptContent = normalize(promptContent);
    metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
  }

  private static String normalize(String value) {
    return StringUtils.defaultString(value).trim();
  }
}
