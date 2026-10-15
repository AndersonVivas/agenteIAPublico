package com.pichincha.cdemsaspopenaidocumentation.copilot.domain;

import java.util.Map;

public record DocumentStateSnapshot(Map<String, String> sectionFingerprints) {

  public DocumentStateSnapshot {
    sectionFingerprints = sectionFingerprints == null ? Map.of() : Map.copyOf(sectionFingerprints);
  }

  public static DocumentStateSnapshot empty() {
    return new DocumentStateSnapshot(Map.of());
  }
}
