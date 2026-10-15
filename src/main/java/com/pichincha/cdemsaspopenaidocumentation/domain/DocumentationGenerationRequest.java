package com.pichincha.cdemsaspopenaidocumentation.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import org.apache.commons.lang3.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = false)
public record DocumentationGenerationRequest(
    @NotBlank String scanId,
    @NotBlank String templateId) {

  public DocumentationGenerationRequest {
    scanId = StringUtils.defaultString(scanId).trim();
    templateId = StringUtils.defaultString(templateId).trim();
  }
}