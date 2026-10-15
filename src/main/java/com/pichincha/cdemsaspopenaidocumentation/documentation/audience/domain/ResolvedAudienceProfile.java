package com.pichincha.cdemsaspopenaidocumentation.documentation.audience.domain;

import java.util.List;

public record ResolvedAudienceProfile(
    AudienceProfile profile,
    AudienceType resolvedType,
    String resolutionSource,
    boolean fallbackApplied,
    List<String> warnings) {

  public ResolvedAudienceProfile {
    warnings = warnings == null ? List.of() : List.copyOf(warnings);
  }
}
