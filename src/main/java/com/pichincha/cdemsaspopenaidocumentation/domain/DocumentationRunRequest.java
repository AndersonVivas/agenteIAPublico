package com.pichincha.cdemsaspopenaidocumentation.domain;

import java.util.List;

public record DocumentationRunRequest(
    RepositoryCoordinates repository,
    Long pullRequestId,
    List<String> templateIds,
    boolean forceFullScan,
    boolean generateDocumentation) {

  public DocumentationRunRequest {
    templateIds = templateIds == null ? List.of() : List.copyOf(templateIds);
  }
}
