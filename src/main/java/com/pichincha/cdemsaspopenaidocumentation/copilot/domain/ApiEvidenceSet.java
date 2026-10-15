package com.pichincha.cdemsaspopenaidocumentation.copilot.domain;

import com.pichincha.cdemsaspopenaidocumentation.domain.CodeSignalEvidence;
import java.util.List;

public record ApiEvidenceSet(List<CodeSignalEvidence> evidences) {

  public ApiEvidenceSet {
    evidences = evidences == null ? List.of() : List.copyOf(evidences);
  }
}