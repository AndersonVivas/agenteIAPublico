package com.pichincha.cdemsaspopenaidocumentation.copilot.domain;

import java.time.Instant;
import org.apache.commons.lang3.StringUtils;

public record DocumentProvenance(
    String scanId,
    String organization,
    String projectName,
    String repositoryName,
    String repositoryPath,
    String templateId,
    String templateVersion,
    String resolvedFrom,
    String audienceType,
    String communicationProfileId,
    int selectedFindings,
    int excludedFindings,
    int sectionCount,
    int evidenceCount,
    Instant generatedAt) {

  public DocumentProvenance {
    scanId = normalize(scanId);
    organization = normalize(organization);
    projectName = normalize(projectName);
    repositoryName = normalize(repositoryName);
    repositoryPath = normalize(repositoryPath);
    templateId = normalize(templateId);
    templateVersion = normalize(templateVersion);
    resolvedFrom = normalize(resolvedFrom);
    audienceType = normalize(audienceType);
    communicationProfileId = normalize(communicationProfileId);
    generatedAt = generatedAt == null ? Instant.now() : generatedAt;
  }

  private static String normalize(String value) {
    return StringUtils.defaultString(value).trim();
  }
}
