package com.pichincha.cdemsaspopenaidocumentation.domain;

public record ScanCommand(
    String scanId,
    RepositoryCoordinates repository,
    Long pullRequestId,
    boolean forceFullScan) {
}
