package com.pichincha.cdemsaspopenaidocumentation.domain;

import java.nio.file.Path;

public record RepositoryCloneResult(
    Path localPath,
    String headCommit,
    String checkedOutRef,
    boolean pullRequestResolved) {
}
