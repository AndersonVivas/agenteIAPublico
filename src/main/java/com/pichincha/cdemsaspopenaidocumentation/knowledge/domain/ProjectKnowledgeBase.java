package com.pichincha.cdemsaspopenaidocumentation.knowledge.domain;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public record ProjectKnowledgeBase(
    ApiKnowledge apiKnowledge,
    ExceptionKnowledge exceptionKnowledge,
    DependencyKnowledge dependencyKnowledge,
    ConfigurationKnowledge configurationKnowledge,
    MonitoringKnowledge monitoringKnowledge,
    BusinessKnowledge businessKnowledge,
    List<KnowledgeGap> knowledgeGaps,
    List<KnowledgeConflict> knowledgeConflicts) {

  public ProjectKnowledgeBase {
    apiKnowledge = apiKnowledge == null ? new ApiKnowledge(List.of()) : apiKnowledge;
    exceptionKnowledge = exceptionKnowledge == null
        ? new ExceptionKnowledge(List.of()) : exceptionKnowledge;
    dependencyKnowledge = dependencyKnowledge == null
        ? new DependencyKnowledge(List.of()) : dependencyKnowledge;
    configurationKnowledge = configurationKnowledge == null
        ? new ConfigurationKnowledge(List.of()) : configurationKnowledge;
    monitoringKnowledge = monitoringKnowledge == null
        ? new MonitoringKnowledge(List.of()) : monitoringKnowledge;
    businessKnowledge = businessKnowledge == null
        ? new BusinessKnowledge(List.of()) : businessKnowledge;
    knowledgeGaps = orderedGaps(knowledgeGaps);
    knowledgeConflicts = orderedConflicts(knowledgeConflicts);
  }

  public List<KnowledgeFinding> allFindings() {
    return Stream.of(apiKnowledge.findings(), exceptionKnowledge.findings(),
        dependencyKnowledge.findings(), configurationKnowledge.findings(),
        monitoringKnowledge.findings(), businessKnowledge.findings()).flatMap(List::stream)
        .sorted(Comparator.comparing(KnowledgeFinding::findingId)).toList();
  }

  private static List<KnowledgeGap> orderedGaps(List<KnowledgeGap> gaps) {
    if (gaps == null || gaps.isEmpty()) {
      return List.of();
    }
    return gaps.stream().distinct().sorted(Comparator.comparing(KnowledgeGap::gapId)).toList();
  }

  private static List<KnowledgeConflict> orderedConflicts(List<KnowledgeConflict> conflicts) {
    if (conflicts == null || conflicts.isEmpty()) {
      return List.of();
    }
    return conflicts.stream().distinct()
        .sorted(Comparator.comparing(KnowledgeConflict::conflictId)).toList();
  }
}
