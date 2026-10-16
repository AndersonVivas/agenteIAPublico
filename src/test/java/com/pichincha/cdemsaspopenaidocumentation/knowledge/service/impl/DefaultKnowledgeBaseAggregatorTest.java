package com.pichincha.cdemsaspopenaidocumentation.knowledge.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.ApiKnowledge;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.ConfigurationKnowledge;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.DependencyKnowledge;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.EvidenceRef;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.ExceptionKnowledge;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.KnowledgeFinding;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.MonitoringKnowledge;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.ProjectKnowledgeBase;
import java.util.List;
import org.junit.jupiter.api.Test;

class DefaultKnowledgeBaseAggregatorTest {

  @Test
  void shouldCompactDuplicatedFindingsAndKeepEvidenceTraceability() {
    DefaultKnowledgeBaseAggregator aggregator = new DefaultKnowledgeBaseAggregator();
    KnowledgeFinding first = finding("f1", "api", "k1", 0.6,
        List.of(ref("e1"), ref("e2")));
    KnowledgeFinding second = finding("f2", "api", "k1", 0.8,
        List.of(ref("e2"), ref("e3")));
    ProjectKnowledgeBase projected = new ProjectKnowledgeBase(
        new ApiKnowledge(List.of(first, second)),
        new ExceptionKnowledge(List.of()),
        new DependencyKnowledge(List.of()),
        new ConfigurationKnowledge(List.of()),
        new MonitoringKnowledge(List.of()),
        null, List.of(), List.of());

    ProjectKnowledgeBase aggregated = aggregator.aggregate(projected);

    assertEquals(1, aggregated.apiKnowledge().findings().size());
    assertEquals(3, aggregated.apiKnowledge().findings().get(0).evidenceRefs().size());
    assertEquals(0.8, aggregated.apiKnowledge().findings().get(0).confidence());
  }

  private KnowledgeFinding finding(String id, String category, String key,
      double confidence, List<EvidenceRef> refs) {
    return new KnowledgeFinding(id, category, key, "title", "summary", "medium",
        confidence, refs);
  }

  private EvidenceRef ref(String id) {
    return new EvidenceRef(id, "A.java", "RULE", "api", 0.9);
  }
}
