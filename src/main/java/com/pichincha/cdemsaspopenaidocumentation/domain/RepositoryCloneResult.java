package com.pichincha.cdemsaspopenaidocumentation.domain;

import java.nio.file.Path;

public record RepositoryCloneResult(
    String repositoryName,
    Long pullRequestId,
    String localPath,
    String status) {

  public static RepositoryCloneResult cloned(String repositoryName, Long pullRequestId,
      Path localPath) {
    return new RepositoryCloneResult(repositoryName, pullRequestId, localPath.toString(),
        "CLONED");
  }
}