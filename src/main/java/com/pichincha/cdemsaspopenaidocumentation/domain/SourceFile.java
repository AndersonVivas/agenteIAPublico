package com.pichincha.cdemsaspopenaidocumentation.domain;

import java.util.List;

public record SourceFile(
    String relativePath,
    String fileName,
    String extension,
    String language,
    String sourceKind,
    List<String> lines) {

  public SourceFile {
    lines = lines == null ? List.of() : List.copyOf(lines);
  }

  public String line(int index) {
    return lines.get(index);
  }
}
