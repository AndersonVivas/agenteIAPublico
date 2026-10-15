package com.pichincha.cdemsaspopenaidocumentation.copilot.domain;

import com.pichincha.cdemsaspopenaidocumentation.domain.CodeSignalEvidence;
import java.util.List;

public record ConfigurationEvidenceSet(List<CodeSignalEvidence> evidences) {

  public ConfigurationEvidenceSet {
    evidences = evidences == null ? List.of() : List.copyOf(evidences);
  }
}