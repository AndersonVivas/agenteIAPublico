package com.pichincha.cdemsaspopenaidocumentation.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RepositoryCloneRequest(
    @NotBlank String organization,
    @NotBlank String projectName,
    @NotBlank String repositoryName,
    @NotNull @Positive Long pullRequestId) {
}