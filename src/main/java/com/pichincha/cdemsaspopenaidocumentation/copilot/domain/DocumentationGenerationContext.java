package com.pichincha.cdemsaspopenaidocumentation.copilot.domain;

import com.pichincha.cdemsaspopenaidocumentation.domain.CodeScanSummary;
import org.apache.commons.lang3.StringUtils;

public record DocumentationGenerationContext(
    String scanId,
    String repositoryName,
    String repositoryPath,
    String organization,
    String projectName,
    Long pullRequestId,
    CodeScanSummary codeScanSummary) {

  public DocumentationGenerationContext {
    scanId = normalize(scanId);
    repositoryName = normalize(repositoryName);
    repositoryPath = normalize(repositoryPath);
    organization = normalize(organization);
    projectName = normalize(projectName);
  }

  private static String normalize(String value) {
    return StringUtils.defaultString(value).trim();
  }
}