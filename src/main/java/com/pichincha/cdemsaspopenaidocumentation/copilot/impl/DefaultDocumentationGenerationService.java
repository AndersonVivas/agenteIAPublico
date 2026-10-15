package com.pichincha.cdemsaspopenaidocumentation.copilot.impl;

import com.pichincha.cdemsaspopenaidocumentation.copilot.DocumentationContextRegistry;
import com.pichincha.cdemsaspopenaidocumentation.copilot.DocumentationGenerationService;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentContext;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentationGenerationContext;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.PromptExecutionResult;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.service.TemplateRegistryService;
import com.pichincha.cdemsaspopenaidocumentation.domain.DocumentationGenerationRequest;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.ProjectKnowledgeBase;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.service.KnowledgeBaseAggregator;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.service.KnowledgeProjectionService;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Entry point of the documentation generation port. Projects and aggregates the project
 * knowledge base, resolves the template and rendering plan, then delegates the actual
 * multi-agent pipeline execution (evidence partitioning, specialist agents, consolidation,
 * template rendering, incremental assembly and persistence) to the
 * {@code MultiAgentDocumentationOrchestrator}.
 */
@Service
public class DefaultDocumentationGenerationService implements DocumentationGenerationService {

  private final DocumentationContextRegistry contextRegistry;
  private final DocumentContextFactory documentContextFactory;
  private final KnowledgeProjectionService knowledgeProjectionService;
  private final KnowledgeBaseAggregator knowledgeBaseAggregator;
  private final TemplateRegistryService templateRegistryService;
  private final MultiAgentDocumentationOrchestrator orchestrator;

  public DefaultDocumentationGenerationService(DocumentationContextRegistry contextRegistry,
      DocumentContextFactory documentContextFactory,
      KnowledgeProjectionService knowledgeProjectionService,
      KnowledgeBaseAggregator knowledgeBaseAggregator,
      TemplateRegistryService templateRegistryService,
      MultiAgentDocumentationOrchestrator orchestrator) {
    this.contextRegistry = contextRegistry;
    this.documentContextFactory = documentContextFactory;
    this.knowledgeProjectionService = knowledgeProjectionService;
    this.knowledgeBaseAggregator = knowledgeBaseAggregator;
    this.templateRegistryService = templateRegistryService;
    this.orchestrator = orchestrator;
  }

  @Override
  public PromptExecutionResult generate(DocumentationGenerationRequest request) {
    DocumentationGenerationContext context = contextRegistry.findByScanId(request.scanId())
        .orElseThrow(() -> new IllegalStateException(
            "No server-side scan context was found for scanId: " + request.scanId()));
    templateRegistryService.registerAvailableTemplates();
    ProjectKnowledgeBase projected = knowledgeProjectionService.project(
        context.codeScanSummary(), context.codeScanSummary() == null
            ? List.of() : context.codeScanSummary().evidences());
    ProjectKnowledgeBase aggregated = knowledgeBaseAggregator.aggregate(projected);
    DocumentContext documentContext = documentContextFactory.create(context, aggregated,
        request.templateId());
    return orchestrator.generate(documentContext);
  }
}
