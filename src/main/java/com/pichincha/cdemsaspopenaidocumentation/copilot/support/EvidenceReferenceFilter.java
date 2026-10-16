package com.pichincha.cdemsaspopenaidocumentation.copilot.support;

import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.AgentFinding;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.AgentKnowledgeResult;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.ConsolidatedKnowledge;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Drops evidence identifiers that an LLM response cites but that do not exist in the scanned
 * evidence, and discards findings left without any verifiable evidence, reporting them as
 * warnings so they remain visible in the changelog.
 */
public final class EvidenceReferenceFilter {

  private static final String DISCARDED_FINDING = "Finding discarded, no verifiable evidence: ";

  private EvidenceReferenceFilter() {
  }

  public static AgentKnowledgeResult retainKnownEvidence(AgentKnowledgeResult result,
      Set<String> knownEvidenceIds) {
    VerifiedFindings verified = verify(result.findings(), knownEvidenceIds);
    return new AgentKnowledgeResult(result.agentType(), verified.findings(), result.gaps(),
        merge(result.warnings(), verified.warnings()), result.modelUsed());
  }

  public static ConsolidatedKnowledge retainKnownEvidence(ConsolidatedKnowledge knowledge,
      Set<String> knownEvidenceIds) {
    VerifiedFindings verified = verify(knowledge.findings(), knownEvidenceIds);
    return new ConsolidatedKnowledge(verified.findings(), knowledge.unresolvedConflicts(),
        merge(knowledge.warnings(), verified.warnings()));
  }

  private static VerifiedFindings verify(List<AgentFinding> findings, Set<String> knownIds) {
    List<AgentFinding> kept = new ArrayList<>();
    List<String> warnings = new ArrayList<>();
    for (AgentFinding finding : findings) {
      AgentFinding verified = withKnownEvidence(finding, knownIds);
      if (verified.evidenceIds().isEmpty()) {
        warnings.add(DISCARDED_FINDING + finding.title());
      } else {
        kept.add(verified);
      }
    }
    return new VerifiedFindings(kept, warnings);
  }

  private static AgentFinding withKnownEvidence(AgentFinding finding, Set<String> knownIds) {
    return new AgentFinding(finding.findingId(), finding.title(), finding.businessSummary(),
        finding.technicalSummary(), finding.severity(),
        finding.evidenceIds().stream().filter(knownIds::contains).toList());
  }

  private static List<String> merge(List<String> first, List<String> second) {
    List<String> merged = new ArrayList<>(first);
    merged.addAll(second);
    return merged;
  }

  private record VerifiedFindings(List<AgentFinding> findings, List<String> warnings) {
  }
}
