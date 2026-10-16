package com.pichincha.cdemsaspopenaidocumentation.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

public record RepositoryCloneRequest(
    @NotBlank String organization,
    @NotBlank String projectName,
    @NotBlank String repositoryName,
    @NotNull @Positive Long pullRequestId,
    List<String> templateIds,
    boolean forceFullScan) {

  public RepositoryCloneRequest {
    templateIds = templateIds == null ? List.of() : List.copyOf(templateIds);
  }

  public RepositoryCoordinates coordinates() {
    return new RepositoryCoordinates(organization, projectName, repositoryName);
  }
}
