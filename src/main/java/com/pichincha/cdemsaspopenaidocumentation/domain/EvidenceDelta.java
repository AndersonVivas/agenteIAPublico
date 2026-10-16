package com.pichincha.cdemsaspopenaidocumentation.domain;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public record EvidenceDelta(List<CodeSignalEvidence> added, List<CodeSignalEvidence> removed) {

  public EvidenceDelta {
    added = added == null ? List.of() : List.copyOf(added);
    removed = removed == null ? List.of() : List.copyOf(removed);
  }

  public static EvidenceDelta between(List<CodeSignalEvidence> superseded,
      List<CodeSignalEvidence> introduced) {
    Set<String> previousIds = idsOf(superseded);
    Set<String> currentIds = idsOf(introduced);
    return new EvidenceDelta(
        introduced.stream().filter(item -> !previousIds.contains(item.evidenceId())).toList(),
        superseded.stream().filter(item -> !currentIds.contains(item.evidenceId())).toList());
  }

  private static Set<String> idsOf(List<CodeSignalEvidence> evidences) {
    return evidences.stream().map(CodeSignalEvidence::evidenceId).collect(Collectors.toSet());
  }
}
