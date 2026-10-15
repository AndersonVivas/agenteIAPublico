package com.pichincha.cdemsaspopenaidocumentation.copilot.impl;

import com.pichincha.cdemsaspopenaidocumentation.copilot.ChangelogService;
import com.pichincha.cdemsaspopenaidocumentation.copilot.ConsolidationAgent;
import com.pichincha.cdemsaspopenaidocumentation.copilot.DocumentPersistenceService;
import com.pichincha.cdemsaspopenaidocumentation.copilot.DocumentStateRepository;
import com.pichincha.cdemsaspopenaidocumentation.copilot.EvidencePartitionService;
import com.pichincha.cdemsaspopenaidocumentation.copilot.MarkdownDocumentService;
import com.pichincha.cdemsaspopenaidocumentation.copilot.RepositoryCommitResolver;
import com.pichincha.cdemsaspopenaidocumentation.copilot.RepositoryDocumentResolver;
import com.pichincha.cdemsaspopenaidocumentation.copilot.SpecialistDocumentationAgent;
import com.pichincha.cdemsaspopenaidocumentation.copilot.TemplateRenderAgent;
import com.pichincha.cdemsaspopenaidocumentation.copilot.config.DocumentationProperties;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.AgentKnowledgeResult;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.ChangelogEntry;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.ConsolidatedKnowledge;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.ConsolidationRequest;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentContext;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentStateSnapshot;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentationAgentType;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentationGenerationContext;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.EvidencePartitions;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.PromptExecutionResult;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.PromptExecutionStatus;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.RepositoryDocumentSnapshot;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.SpecialistAgentRequest;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.TemplateRenderRequest;
import com.pichincha.cdemsaspopenaidocumentation.copilot.support.SectionContextCategoryMatcher;
import com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.domain.SectionContext;
import com.pichincha.cdemsaspopenaidocumentation.domain.CodeSignalEvidence;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * Coordinates the full multi-agent documentation pipeline: evidence partitioning,
 * specialist agent execution scoped to only the sections that actually changed,
 * knowledge consolidation, Markdown rendering for impacted sections only, deterministic
 * document assembly against the previous version, and persistence of the document, its
 * incremental state snapshot and its changelog entry.
 */
@Service
public class MultiAgentDocumentationOrchestrator {

  private static final String PROVIDER = "github-copilot-cli";

  private final EvidencePartitionService evidencePartitionService;
  private final List<SpecialistDocumentationAgent> specialistAgents;
  private final ConsolidationAgent consolidationAgent;
  private final TemplateRenderAgent templateRenderAgent;
  private final DocumentStateRepository documentStateRepository;
  private final ChangelogService changelogService;
  private final DocumentPersistenceService documentPersistenceService;
  private final MarkdownDocumentService markdownDocumentService;
  private final RepositoryDocumentResolver repositoryDocumentResolver;
  private final RepositoryCommitResolver repositoryCommitResolver;
  private final DocumentationProperties documentationProperties;
  private final IncrementalSectionPlanner incrementalSectionPlanner = new IncrementalSectionPlanner();
  private final MarkdownSectionParser markdownSectionParser = new MarkdownSectionParser();
  private final DocumentAssembler documentAssembler = new DocumentAssembler();

  public MultiAgentDocumentationOrchestrator(EvidencePartitionService evidencePartitionService,
      List<SpecialistDocumentationAgent> specialistAgents, ConsolidationAgent consolidationAgent,
      TemplateRenderAgent templateRenderAgent, DocumentStateRepository documentStateRepository,
      ChangelogService changelogService, DocumentPersistenceService documentPersistenceService,
      MarkdownDocumentService markdownDocumentService,
      RepositoryDocumentResolver repositoryDocumentResolver,
      RepositoryCommitResolver repositoryCommitResolver,
      DocumentationProperties documentationProperties) {
    this.evidencePartitionService = evidencePartitionService;
    this.specialistAgents = List.copyOf(specialistAgents);
    this.consolidationAgent = consolidationAgent;
    this.templateRenderAgent = templateRenderAgent;
    this.documentStateRepository = documentStateRepository;
    this.changelogService = changelogService;
    this.documentPersistenceService = documentPersistenceService;
    this.markdownDocumentService = markdownDocumentService;
    this.repositoryDocumentResolver = repositoryDocumentResolver;
    this.repositoryCommitResolver = repositoryCommitResolver;
    this.documentationProperties = documentationProperties;
  }

  public PromptExecutionResult generate(DocumentContext documentContext) {
    Instant start = Instant.now();
    DocumentationGenerationContext generationContext = documentContext.generationContext();
    String documentName = markdownDocumentService.buildDocumentName(
        generationContext.repositoryName());
    List<SectionContext> allSections = documentContext.renderingPlan().sectionContexts();

    Map<String, String> previousBodiesByTitle = loadPreviousBodies(generationContext,
        documentName);
    List<SectionContext> impactedSections = resolveImpactedSections(generationContext,
        documentName, allSections);

    EvidencePartitions partitions = evidencePartitionService.partition(
        generationContext.codeScanSummary());
    List<AgentKnowledgeResult> specialistResults = runSpecialistAgents(documentContext,
        generationContext, partitions, impactedSections);

    ConsolidatedKnowledge consolidatedKnowledge = ConsolidatedKnowledge.empty();
    Map<String, String> renderedBodies = Map.of();
    if (!impactedSections.isEmpty()) {
      consolidatedKnowledge = consolidationAgent.consolidate(new ConsolidationRequest(
          generationContext, specialistResults, collectGaps(specialistResults), List.of()));
      renderedBodies = templateRenderAgent.render(new TemplateRenderRequest(generationContext,
          documentContext.resolvedTemplateContext(), impactedSections, consolidatedKnowledge,
          documentContext.outputContract()));
    }

    String assembledDocument = documentAssembler.assemble(
        documentContext.resolvedTemplateContext().template().name(), allSections,
        renderedBodies, previousBodiesByTitle);

    Path documentPath = persist(generationContext, documentName, assembledDocument);
    persistState(generationContext, documentName, allSections);
    recordChangelog(generationContext, impactedSections, specialistResults,
        consolidatedKnowledge);

    long executionTime = Duration.between(start, Instant.now()).toMillis();
    return new PromptExecutionResult(documentContext.documentId(), assembledDocument,
        documentPath.toString(), documentName, PromptExecutionStatus.SUCCESS, executionTime,
        PROVIDER, Instant.now());
  }

  private Map<String, String> loadPreviousBodies(DocumentationGenerationContext generationContext,
      String documentName) {
    Optional<RepositoryDocumentSnapshot> previousDocument = repositoryDocumentResolver.resolve(
        generationContext.repositoryPath(), documentationProperties.outputDirectory(),
        documentName);
    return markdownSectionParser.parseByTitle(
        previousDocument.map(RepositoryDocumentSnapshot::content).orElse(""));
  }

  private List<SectionContext> resolveImpactedSections(
      DocumentationGenerationContext generationContext, String documentName,
      List<SectionContext> allSections) {
    if (!documentationProperties.evolutionaryMode()) {
      return allSections;
    }
    DocumentStateSnapshot previousState = documentStateRepository.load(
        generationContext.repositoryPath(), documentationProperties.outputDirectory(),
        documentationProperties.centralizedOutputPath(), documentName)
        .orElse(DocumentStateSnapshot.empty());
    return incrementalSectionPlanner.impactedSections(allSections, previousState);
  }

  private List<AgentKnowledgeResult> runSpecialistAgents(DocumentContext documentContext,
      DocumentationGenerationContext generationContext, EvidencePartitions partitions,
      List<SectionContext> impactedSections) {
    if (impactedSections.isEmpty()) {
      return List.of();
    }
    List<AgentKnowledgeResult> results = new ArrayList<>();
    for (SpecialistDocumentationAgent agent : specialistAgents) {
      List<CodeSignalEvidence> evidence = evidenceFor(agent.agentType(), partitions);
      if (evidence.isEmpty()) {
        continue;
      }
      List<SectionContext> relevantSections = SectionContextCategoryMatcher.match(
          agent.agentType(), impactedSections);
      SpecialistAgentRequest request = new SpecialistAgentRequest(documentContext.documentId(),
          generationContext, evidence, relevantSections);
      results.add(agent.execute(request));
    }
    return List.copyOf(results);
  }

  private List<String> collectGaps(List<AgentKnowledgeResult> results) {
    return results.stream().flatMap(result -> result.gaps().stream()).distinct().toList();
  }

  private Path persist(DocumentationGenerationContext generationContext, String documentName,
      String assembledDocument) {
    return documentPersistenceService.save(generationContext.repositoryPath(),
        documentationProperties.outputDirectory(), documentationProperties.centralizedOutputPath(),
        documentName, assembledDocument, documentationProperties.overwriteExisting());
  }

  private void persistState(DocumentationGenerationContext generationContext, String documentName,
      List<SectionContext> allSections) {
    DocumentStateSnapshot newState = incrementalSectionPlanner.buildSnapshot(allSections);
    documentStateRepository.save(generationContext.repositoryPath(),
        documentationProperties.outputDirectory(), documentationProperties.centralizedOutputPath(),
        documentName, newState);
  }

  private void recordChangelog(DocumentationGenerationContext generationContext,
      List<SectionContext> impactedSections, List<AgentKnowledgeResult> specialistResults,
      ConsolidatedKnowledge consolidatedKnowledge) {
    String commitHash = repositoryCommitResolver
        .resolveCommitHash(Path.of(generationContext.repositoryPath())).orElse("unknown");
    List<String> updatedSectionTitles = impactedSections.stream()
        .map(SectionContext::title).toList();
    List<String> newEvidenceIds = specialistResults.stream()
        .flatMap(result -> result.findings().stream())
        .flatMap(finding -> finding.evidenceIds().stream()).distinct().toList();
    changelogService.record(generationContext.repositoryPath(),
        documentationProperties.outputDirectory(), documentationProperties.centralizedOutputPath(),
        new ChangelogEntry(Instant.now(), generationContext.repositoryName(), commitHash,
            generationContext.pullRequestId(), consolidatedKnowledge.warnings(),
            updatedSectionTitles, newEvidenceIds));
  }

  private List<CodeSignalEvidence> evidenceFor(DocumentationAgentType agentType,
      EvidencePartitions partitions) {
    return switch (agentType) {
      case API -> partitions.api().evidences();
      case EXCEPTION -> partitions.exceptions().evidences();
      case DEPENDENCY -> partitions.dependencies().evidences();
      case CONFIGURATION -> partitions.configuration().evidences();
      case MONITORING -> partitions.monitoring().evidences();
      case BUSINESS_FLOW -> partitions.businessFlow().evidences();
    };
  }
}
