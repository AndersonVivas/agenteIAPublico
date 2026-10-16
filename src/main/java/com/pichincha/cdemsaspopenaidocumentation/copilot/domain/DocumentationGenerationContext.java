package com.pichincha.cdemsaspopenaidocumentation.copilot.domain;

import com.pichincha.cdemsaspopenaidocumentation.domain.CodeSignalEvidence;
import com.pichincha.cdemsaspopenaidocumentation.domain.FileChange;
import com.pichincha.cdemsaspopenaidocumentation.domain.RepositoryCoordinates;
import com.pichincha.cdemsaspopenaidocumentation.domain.ScanExecutionSummary;
import java.util.List;

public record DocumentationGenerationContext(
    RepositoryCoordinates repository,
    Long pullRequestId,
    ScanExecutionSummary scan,
    List<CodeSignalEvidence> evidences,
    List<FileChange> changedFiles) {

  public DocumentationGenerationContext {
    evidences = evidences == null ? List.of() : List.copyOf(evidences);
    changedFiles = changedFiles == null ? List.of() : List.copyOf(changedFiles);
  }
}
