package com.pichincha.cdemsaspopenaidocumentation.copilot.domain;

import com.pichincha.cdemsaspopenaidocumentation.domain.CodeSignalEvidence;
import java.util.List;

public record DependencyEvidenceSet(List<CodeSignalEvidence> evidences) {

  public DependencyEvidenceSet {
    evidences = evidences == null ? List.of() : List.copyOf(evidences);
  }
}