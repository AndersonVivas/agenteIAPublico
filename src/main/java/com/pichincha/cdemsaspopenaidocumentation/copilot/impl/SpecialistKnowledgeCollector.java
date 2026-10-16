package com.pichincha.cdemsaspopenaidocumentation.copilot.impl;

import com.pichincha.cdemsaspopenaidocumentation.copilot.EvidencePartitionService;
import com.pichincha.cdemsaspopenaidocumentation.copilot.SpecialistDocumentationAgent;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.AgentKnowledgeResult;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentContext;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.EvidencePartitions;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.SpecialistAgentRequest;
import com.pichincha.cdemsaspopenaidocumentation.copilot.support.SectionContextCategoryMatcher;
import com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.domain.SectionContext;
import com.pichincha.cdemsaspopenaidocumentation.domain.CodeSignalEvidence;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Runs every specialist agent that has evidence of its category, scoped to the impacted
 * sections that depend on that category, and collects their structured knowledge.
 */
@Component
@RequiredArgsConstructor
public class SpecialistKnowledgeCollector {

  private final EvidencePartitionService evidencePartitionService;
  private final List<SpecialistDocumentationAgent> specialistAgents;

  public List<AgentKnowledgeResult> collect(DocumentContext context,
      List<SectionContext> impactedSections) {
    EvidencePartitions partitions = evidencePartitionService.partition(
        context.generationContext().evidences());
    return specialistAgents.stream()
        .map(agent -> run(agent, context, partitions, impactedSections))
        .flatMap(Optional::stream)
        .toList();
  }

  private Optional<AgentKnowledgeResult> run(SpecialistDocumentationAgent agent,
      DocumentContext context, EvidencePartitions partitions,
      List<SectionContext> impactedSections) {
    List<CodeSignalEvidence> evidence = partitions.evidenceFor(agent.agentType());
    if (evidence.isEmpty()) {
      return Optional.empty();
    }
    List<SectionContext> relevantSections = SectionContextCategoryMatcher.match(
        agent.agentType(), impactedSections);
    return Optional.of(agent.execute(new SpecialistAgentRequest(context.generationContext(),
        evidence, relevantSections)));
  }
}
