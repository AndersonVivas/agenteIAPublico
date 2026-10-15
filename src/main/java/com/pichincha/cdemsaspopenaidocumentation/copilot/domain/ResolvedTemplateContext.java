package com.pichincha.cdemsaspopenaidocumentation.copilot.domain;

import com.pichincha.cdemsaspopenaidocumentation.documentation.audience.domain.AudienceProfile;
import com.pichincha.cdemsaspopenaidocumentation.documentation.communication.domain.CommunicationProfile;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.DocumentTemplate;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateVersion;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

public record ResolvedTemplateContext(
    DocumentTemplate template,
    TemplateVersion requestedVersion,
    TemplateVersion resolvedVersion,
    String resolvedFrom,
    boolean fallbackApplied,
    AudienceProfile audienceProfile,
    CommunicationProfile communicationProfile,
    List<String> warnings) {

  public ResolvedTemplateContext {
    if (template == null) {
      throw new IllegalArgumentException("Resolved template is required.");
    }
    requestedVersion = requestedVersion == null ? template.version() : requestedVersion;
    resolvedVersion = resolvedVersion == null ? template.version() : resolvedVersion;
    resolvedFrom = StringUtils.defaultIfBlank(resolvedFrom, "unknown").trim();
    if (audienceProfile == null) {
      throw new IllegalArgumentException("Audience profile is required.");
    }
    if (communicationProfile == null) {
      throw new IllegalArgumentException("Communication profile is required.");
    }
    warnings = warnings == null ? List.of() : List.copyOf(warnings);
  }
}
