package com.pichincha.cdemsaspopenaidocumentation.knowledge.service.impl;

import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.ApiKnowledge;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.ConfigurationKnowledge;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.DependencyKnowledge;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.EvidenceRef;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.ExceptionKnowledge;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.KnowledgeConflict;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.KnowledgeFinding;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.KnowledgeGap;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.MonitoringKnowledge;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.ProjectKnowledgeBase;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.service.KnowledgeBaseAggregator;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;

@Service
public class DefaultKnowledgeBaseAggregator implements KnowledgeBaseAggregator {

  @Override
  public ProjectKnowledgeBase aggregate(ProjectKnowledgeBase projectedKnowledge) {
    ApiKnowledge api = new ApiKnowledge(compact(projectedKnowledge.apiKnowledge().findings()));
    ExceptionKnowledge exceptions = new ExceptionKnowledge(
        compact(projectedKnowledge.exceptionKnowledge().findings()));
    DependencyKnowledge dependencies = new DependencyKnowledge(
        compact(projectedKnowledge.dependencyKnowledge().findings()));
    ConfigurationKnowledge configuration = new ConfigurationKnowledge(
        compact(projectedKnowledge.configurationKnowledge().findings()));
    MonitoringKnowledge monitoring = new MonitoringKnowledge(
        compact(projectedKnowledge.monitoringKnowledge().findings()));
    Map<String, EvidenceRef> index = evidenceIndex(api, exceptions, dependencies,
        configuration, monitoring, projectedKnowledge);
    return new ProjectKnowledgeBase(projectedKnowledge.repositoryContext(), api,
        exceptions, dependencies, configuration, monitoring,
        projectedKnowledge.knowledgeGaps(), projectedKnowledge.knowledgeConflicts(), index);
  }

  private List<KnowledgeFinding> compact(List<KnowledgeFinding> findings) {
    return findings.stream().collect(Collectors.toMap(KnowledgeFinding::normalizedKey,
        finding -> finding, this::mergeFinding, LinkedHashMap::new)).values().stream()
        .sorted(Comparator.comparing(KnowledgeFinding::findingId)).toList();
  }

  private KnowledgeFinding mergeFinding(KnowledgeFinding first, KnowledgeFinding second) {
    List<EvidenceRef> refs = Stream.concat(first.evidenceRefs().stream(),
        second.evidenceRefs().stream()).distinct()
        .sorted(Comparator.comparing(EvidenceRef::evidenceId)).toList();
    double confidence = Math.max(first.confidence(), second.confidence());
    return new KnowledgeFinding(first.findingId(), first.category(), first.normalizedKey(),
        first.title(), first.summary(), first.severity(), confidence, refs);
  }

  private Map<String, EvidenceRef> evidenceIndex(ApiKnowledge api,
      ExceptionKnowledge exceptions, DependencyKnowledge dependencies,
      ConfigurationKnowledge configuration, MonitoringKnowledge monitoring,
      ProjectKnowledgeBase projectedKnowledge) {
    Map<String, EvidenceRef> index = Stream.of(api.findings(), exceptions.findings(),
        dependencies.findings(), configuration.findings(), monitoring.findings())
        .flatMap(List::stream).flatMap(finding -> finding.evidenceRefs().stream())
        .collect(Collectors.toMap(EvidenceRef::evidenceId, ref -> ref,
            (first, ignored) -> first, LinkedHashMap::new));
    Stream.concat(projectedKnowledge.knowledgeGaps().stream(),
        projectedKnowledge.knowledgeConflicts().stream().map(this::asGap))
        .flatMap(gap -> gap.evidenceRefs().stream())
        .forEach(ref -> index.putIfAbsent(ref.evidenceId(), ref));
    return Map.copyOf(index);
  }

  private KnowledgeGap asGap(KnowledgeConflict conflict) {
    return new KnowledgeGap(conflict.conflictId(), conflict.category(),
        conflict.description(), conflict.evidenceRefs());
  }
}
