package com.pichincha.cdemsaspopenaidocumentation.copilot.domain;

import com.pichincha.cdemsaspopenaidocumentation.domain.CodeSignalEvidence;
import java.util.List;

public record MonitoringEvidenceSet(List<CodeSignalEvidence> evidences) {

  public MonitoringEvidenceSet {
    evidences = evidences == null ? List.of() : List.copyOf(evidences);
  }
}