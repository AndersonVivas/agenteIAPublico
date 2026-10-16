package com.pichincha.cdemsaspopenaidocumentation.copilot.impl;

import com.pichincha.cdemsaspopenaidocumentation.copilot.ConsolidationAgent;
import com.pichincha.cdemsaspopenaidocumentation.copilot.TemplateRenderAgent;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.AgentKnowledgeResult;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.ConsolidatedKnowledge;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.ConsolidationRequest;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentContext;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.PublishedDocument;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.RenderedSections;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.TemplateRenderRequest;
import com.pichincha.cdemsaspopenaidocumentation.copilot.strategy.DocumentLifecycleStrategy;
import com.pichincha.cdemsaspopenaidocumentation.copilot.support.EvidenceReferenceFilter;
import com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.domain.SectionContext;
import com.pichincha.cdemsaspopenaidocumentation.domain.CodeSignalEvidence;
import com.pichincha.cdemsaspopenaidocumentation.domain.DocumentGenerationResult;
import com.pichincha.cdemsaspopenaidocumentation.domain.enums.DocumentGenerationStatusEnum;
import com.pichincha.cdemsaspopenaidocumentation.domain.enums.DocumentLifecycleEnum;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Coordinates the multi-agent pipeline for one document: asks the lifecycle strategy which
 * sections are impacted, runs specialist agents, consolidation, rendering and the optional
 * editorial review only for them, and lets the lifecycle strategy publish the result.
 */
@Service
@RequiredArgsConstructor
public class MultiAgentDocumentationOrchestrator {

  private final List<DocumentLifecycleStrategy> lifecycleStrategies;
  private final SpecialistKnowledgeCollector specialistKnowledgeCollector;
  private final ConsolidationAgent consolidationAgent;
  private final TemplateRenderAgent templateRenderAgent;
  private final EditorialReviewer editorialReviewer;
  private final Clock clock;

  public DocumentGenerationResult generate(DocumentContext context) {
    Instant start = clock.instant();
    DocumentLifecycleStrategy lifecycle = strategyFor(context.lifecycle());
    List<SectionContext> impactedSections = lifecycle.impactedSections(context);
    if (impactedSections.isEmpty()) {
      return result(context, lifecycle.unchangedStatus(), null, start);
    }
    PublishedDocument published = lifecycle.publish(context,
        renderSections(context, impactedSections));
    return result(context, DocumentGenerationStatusEnum.GENERATED, published, start);
  }

  private RenderedSections renderSections(DocumentContext context,
      List<SectionContext> impactedSections) {
    List<AgentKnowledgeResult> specialistResults =
        specialistKnowledgeCollector.collect(context, impactedSections);
    ConsolidatedKnowledge knowledge = EvidenceReferenceFilter.retainKnownEvidence(
        consolidationAgent.consolidate(new ConsolidationRequest(context.generationContext(),
            specialistResults, collectGaps(context, specialistResults),
            context.renderingPlan().knowledgeConflicts())),
        knownEvidenceIds(context));
    Map<String, String> drafts = templateRenderAgent.render(
        new TemplateRenderRequest(context, impactedSections, knowledge));
    return editorialReviewer.review(context, impactedSections,
        new RenderedSections(drafts, collectWarnings(context, specialistResults, knowledge)));
  }

  private DocumentLifecycleStrategy strategyFor(DocumentLifecycleEnum lifecycle) {
    return lifecycleStrategies.stream()
        .filter(strategy -> strategy.lifecycle() == lifecycle)
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("No lifecycle strategy for " + lifecycle));
  }

  private List<String> collectGaps(DocumentContext context,
      List<AgentKnowledgeResult> results) {
    return Stream.concat(context.renderingPlan().knowledgeGaps().stream(),
        results.stream().flatMap(result -> result.gaps().stream())).distinct().toList();
  }

  private List<String> collectWarnings(DocumentContext context,
      List<AgentKnowledgeResult> results, ConsolidatedKnowledge knowledge) {
    return Stream.of(context.renderingPlan().warnings().stream(),
        results.stream().flatMap(result -> result.warnings().stream()),
        knowledge.warnings().stream()).flatMap(warnings -> warnings).distinct().toList();
  }

  private Set<String> knownEvidenceIds(DocumentContext context) {
    return context.generationContext().evidences().stream()
        .map(CodeSignalEvidence::evidenceId).collect(Collectors.toSet());
  }

  private DocumentGenerationResult result(DocumentContext context,
      DocumentGenerationStatusEnum status, PublishedDocument published, Instant start) {
    long elapsedMillis = Duration.between(start, clock.instant()).toMillis();
    if (published == null) {
      return new DocumentGenerationResult(context.template().id(), context.lifecycle(), status,
          null, null, List.of(), elapsedMillis, "No section inputs changed in this scan.");
    }
    return new DocumentGenerationResult(context.template().id(), context.lifecycle(), status,
        published.documentPath(), published.version(), published.updatedSections(),
        elapsedMillis, "Regenerated " + published.updatedSections().size() + " sections.");
  }
}
