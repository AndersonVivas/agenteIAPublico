package com.pichincha.cdemsaspopenaidocumentation.copilot.domain;

import java.nio.file.Path;
import org.apache.commons.lang3.StringUtils;

public record RepositoryDocumentSnapshot(Path path, String content, int version) {

  public RepositoryDocumentSnapshot {
    content = StringUtils.defaultString(content);
    version = Math.max(version, 0);
  }
}
