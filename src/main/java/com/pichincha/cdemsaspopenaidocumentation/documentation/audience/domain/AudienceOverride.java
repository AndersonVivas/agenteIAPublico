package com.pichincha.cdemsaspopenaidocumentation.documentation.audience.domain;

import org.apache.commons.lang3.StringUtils;

public record AudienceOverride(
    AudienceType requestedAudience,
    String preferredLanguageOverride,
    String justification,
    boolean forceIfSupported) {

  public AudienceOverride {
    preferredLanguageOverride = StringUtils.defaultString(preferredLanguageOverride)
        .trim();
    justification = StringUtils.defaultString(justification).trim();
  }

  public boolean hasRequestedAudience() {
    return requestedAudience != null;
  }
}
