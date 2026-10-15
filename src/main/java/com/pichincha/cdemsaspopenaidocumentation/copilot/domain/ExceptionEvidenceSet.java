package com.pichincha.cdemsaspopenaidocumentation.copilot.domain;

import com.pichincha.cdemsaspopenaidocumentation.domain.CodeSignalEvidence;
import java.util.List;

public record ExceptionEvidenceSet(List<CodeSignalEvidence> evidences) {

  public ExceptionEvidenceSet {
    evidences = evidences == null ? List.of() : List.copyOf(evidences);
  }
}