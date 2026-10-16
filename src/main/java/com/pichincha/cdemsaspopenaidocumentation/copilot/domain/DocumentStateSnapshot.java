package com.pichincha.cdemsaspopenaidocumentation.copilot.domain;

import java.util.Map;
import java.util.Optional;

public record DocumentStateSnapshot(Map<String, SectionState> sections) {

  public DocumentStateSnapshot {
    sections = sections == null ? Map.of() : Map.copyOf(sections);
  }

  public static DocumentStateSnapshot empty() {
    return new DocumentStateSnapshot(Map.of());
  }

  public Optional<String> fingerprintOf(String sectionId) {
    return Optional.ofNullable(sections.get(sectionId)).map(SectionState::fingerprint);
  }

  public Optional<String> bodyOf(String sectionId) {
    return Optional.ofNullable(sections.get(sectionId)).map(SectionState::body);
  }
}
