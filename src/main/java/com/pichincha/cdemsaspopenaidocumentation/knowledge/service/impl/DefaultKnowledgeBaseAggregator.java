package com.pichincha.cdemsaspopenaidocumentation.knowledge.service.impl;

import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.ApiKnowledge;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.BusinessKnowledge;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.ConfigurationKnowledge;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.DependencyKnowledge;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.EvidenceRef;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.ExceptionKnowledge;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.KnowledgeFinding;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.MonitoringKnowledge;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.ProjectKnowledgeBase;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.service.KnowledgeBaseAggregator;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
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
    BusinessKnowledge business = new BusinessKnowledge(
        compact(projectedKnowledge.businessKnowledge().findings()));
    return new ProjectKnowledgeBase(api,
        exceptions, dependencies, configuration, monitoring, business,
        projectedKnowledge.knowledgeGaps(), projectedKnowledge.knowledgeConflicts());
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
}
