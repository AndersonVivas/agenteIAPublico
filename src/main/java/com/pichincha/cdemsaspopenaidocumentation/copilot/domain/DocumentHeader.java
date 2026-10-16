package com.pichincha.cdemsaspopenaidocumentation.copilot.domain;

import com.pichincha.cdemsaspopenaidocumentation.domain.enums.DocumentLifecycleEnum;
import java.time.Instant;

public record DocumentHeader(
    String title,
    String templateId,
    String templateVersion,
    DocumentLifecycleEnum lifecycle,
    String repositoryKey,
    int documentVersion,
    String scanId,
    String headCommit,
    Long pullRequestId,
    Instant generatedAt) {
}
