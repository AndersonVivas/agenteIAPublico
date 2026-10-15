package com.pichincha.cdemsaspopenaidocumentation.copilot.domain;

import com.pichincha.cdemsaspopenaidocumentation.domain.CodeSignalEvidence;
import java.util.List;

public record BusinessFlowEvidenceSet(List<CodeSignalEvidence> evidences) {

  public BusinessFlowEvidenceSet {
    evidences = evidences == null ? List.of() : List.copyOf(evidences);
  }
}
