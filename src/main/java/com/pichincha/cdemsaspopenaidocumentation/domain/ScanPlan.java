package com.pichincha.cdemsaspopenaidocumentation.domain;

import com.pichincha.cdemsaspopenaidocumentation.domain.enums.ScanScopeEnum;
import java.util.List;

public record ScanPlan(
    ScanScopeEnum scope,
    String reason,
    List<String> filesToScan,
    List<FileChange> changes) {

  public ScanPlan {
    filesToScan = filesToScan == null ? List.of() : List.copyOf(filesToScan);
    changes = changes == null ? List.of() : List.copyOf(changes);
  }

  public List<String> invalidatedFiles() {
    return changes.stream().filter(FileChange::invalidatesPreviousEvidence)
        .map(FileChange::path).toList();
  }
}
