package com.pichincha.cdemsaspopenaidocumentation.domain;

import java.util.List;

public record ScanRecord(
    ScanCommand command,
    RepositoryCloneResult cloneResult,
    FileManifest manifest,
    ScanPlan plan,
    List<CodeSignalEvidence> evidences,
    String analyzerVersion) {
}
