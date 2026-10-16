package com.pichincha.cdemsaspopenaidocumentation.copilot.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.AgentFinding;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.AgentKnowledgeResult;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.ConsolidatedKnowledge;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentationAgentType;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class EvidenceReferenceFilterTest {

  @Test
  void retainKnownEvidenceShouldDropUnknownIdsAndUnsupportedFindings() {
    AgentKnowledgeResult result = new AgentKnowledgeResult(DocumentationAgentType.API,
        List.of(finding("valid", "EV-1", "EV-562? (no evidence)"), finding("invented", "EV-9")),
        List.of(), List.of(), "model");

    AgentKnowledgeResult filtered = EvidenceReferenceFilter.retainKnownEvidence(result,
        Set.of("EV-1"));

    assertEquals(1, filtered.findings().size());
    assertEquals(List.of("EV-1"), filtered.findings().getFirst().evidenceIds());
    assertTrue(filtered.warnings().getFirst().contains("invented"));
  }

  @Test
  void retainKnownEvidenceShouldFilterConsolidatedKnowledge() {
    ConsolidatedKnowledge knowledge = new ConsolidatedKnowledge(
        List.of(finding("valid", "EV-1"), finding("invented", "EV-X")), List.of(), List.of());

    ConsolidatedKnowledge filtered = EvidenceReferenceFilter.retainKnownEvidence(knowledge,
        Set.of("EV-1"));

    assertEquals(List.of("valid"),
        filtered.findings().stream().map(AgentFinding::title).toList());
    assertEquals(1, filtered.warnings().size());
  }

  private AgentFinding finding(String title, String... evidenceIds) {
    return new AgentFinding(title, title, "business", "technical", "low", List.of(evidenceIds));
  }
}
