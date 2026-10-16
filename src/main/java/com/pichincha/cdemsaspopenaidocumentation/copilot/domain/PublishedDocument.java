package com.pichincha.cdemsaspopenaidocumentation.copilot.domain;

import java.util.List;

public record PublishedDocument(String documentPath, int version, List<String> updatedSections) {

  public PublishedDocument {
    updatedSections = updatedSections == null ? List.of() : List.copyOf(updatedSections);
  }
}
