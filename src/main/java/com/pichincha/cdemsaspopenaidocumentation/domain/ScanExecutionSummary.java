package com.pichincha.cdemsaspopenaidocumentation.domain;

import com.pichincha.cdemsaspopenaidocumentation.domain.enums.ScanScopeEnum;
import java.time.Instant;

public record ScanExecutionSummary(
    String scanId,
    ScanScopeEnum scope,
    String scopeReason,
    String checkedOutRef,
    String headCommit,
    boolean pullRequestResolved,
    String analyzerVersion,
    int filesScanned,
    int changedFiles,
    int evidenceAdded,
    int evidenceRemoved,
    Instant scannedAt) {
}
