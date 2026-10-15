package com.pichincha.cdemsaspopenaidocumentation.copilot.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pichincha.cdemsaspopenaidocumentation.copilot.ChangelogService;
import com.pichincha.cdemsaspopenaidocumentation.copilot.DocumentPersistenceService;
import com.pichincha.cdemsaspopenaidocumentation.copilot.DocumentStateRepository;
import com.pichincha.cdemsaspopenaidocumentation.copilot.DocumentationContextRegistry;
import com.pichincha.cdemsaspopenaidocumentation.copilot.MarkdownDocumentService;
import com.pichincha.cdemsaspopenaidocumentation.copilot.config.DocumentationProperties;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.ConsolidatedKnowledge;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentStateSnapshot;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentationGenerationContext;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.PromptExecutionRequest;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.PromptExecutionResult;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.PromptExecutionStatus;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.RepositoryDocumentSnapshot;
import com.pichincha.cdemsaspopenaidocumentation.documentation.audience.service.AudienceProfileResolver;
import com.pichincha.cdemsaspopenaidocumentation.documentation.communication.service.CommunicationProfileResolver;
import com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.service.DefaultTemplateRenderer;
import com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.service.KnowledgeAdaptationEngine;
import com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.service.KnowledgeSelectionEngine;
import com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.service.SectionPlanningEngine;
import com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.service.TraceabilityAssembler;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.DocumentTemplate;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateMetadata;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateRegistrationResult;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateResolutionResult;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateVersion;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.port.ExternalTemplateSource;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.port.TemplateRepository;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.service.CompositeTemplateSource;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.service.TemplateCatalog;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.service.TemplateRegistryService;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.service.TemplateValidator;
import com.pichincha.cdemsaspopenaidocumentation.domain.CodeScanSummary;
import com.pichincha.cdemsaspopenaidocumentation.domain.DocumentationGenerationRequest;
import com.pichincha.cdemsaspopenaidocumentation.domain.ScanExecutionMetadata;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.service.KnowledgeBaseAggregator;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.service.KnowledgeProjectionService;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.service.impl.DefaultKnowledgeBaseAggregator;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.service.impl.DefaultKnowledgeProjectionService;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class DefaultDocumentationGenerationServiceTest {

  @Test
  void shouldBuildDocumentFromServerSideScanContext() {
    DocumentationGenerationContext context = context();
    DocumentationContextRegistry registry = registryReturning(Optional.of(context));
    AtomicReference<String> capturedTemplateId = new AtomicReference<>();

    DefaultDocumentationGenerationService service = new DefaultDocumentationGenerationService(
        registry, documentContextFactory(), knowledgeProjectionService(),
        knowledgeBaseAggregator(), templateRegistryService(), orchestrator()) {
      @Override
      public PromptExecutionResult generate(DocumentationGenerationRequest request) {
        capturedTemplateId.set(request.templateId());
        return super.generate(request);
      }
    };

    PromptExecutionResult result = service.generate(new DocumentationGenerationRequest(
        "scan-1", "error-manual"));

    assertEquals(PromptExecutionStatus.SUCCESS, result.executionStatus());
    assertEquals("error-manual", capturedTemplateId.get());
    assertTrue(result.documentPath().contains("repo.md"));
  }

  @Test
  void shouldFailWhenScanIdIsUnknown() {
    DocumentationContextRegistry registry = registryReturning(Optional.empty());

    DefaultDocumentationGenerationService service = new DefaultDocumentationGenerationService(
        registry, documentContextFactory(), knowledgeProjectionService(),
        knowledgeBaseAggregator(), templateRegistryService(), orchestrator());

    IllegalStateException exception = assertThrows(IllegalStateException.class,
        () -> service.generate(new DocumentationGenerationRequest("missing-scan",
        "error-manual")));

    assertTrue(exception.getMessage().contains("No server-side scan context"));
  }

  private MultiAgentDocumentationOrchestrator orchestrator() {
    return new MultiAgentDocumentationOrchestrator(new DefaultEvidencePartitionService(),
        List.of(), request -> ConsolidatedKnowledge.empty(), request -> Map.of(),
        fakeDocumentStateRepository(), fakeChangelogService(), fakeDocumentPersistenceService(),
        fakeMarkdownDocumentService(), (repositoryPath, outputDirectory, documentName) ->
            Optional.empty(), repositoryPath -> Optional.of("commit-1"),
        new DocumentationProperties("docs", "", true, true));
  }

  private DocumentStateRepository fakeDocumentStateRepository() {
    return new DocumentStateRepository() {
      @Override
      public Optional<DocumentStateSnapshot> load(String repositoryPath,
          String outputDirectory, String centralizedOutputPath, String documentName) {
        return Optional.empty();
      }

      @Override
      public void save(String repositoryPath, String outputDirectory,
          String centralizedOutputPath, String documentName, DocumentStateSnapshot snapshot) {
      }
    };
  }

  private ChangelogService fakeChangelogService() {
    return (repositoryPath, outputDirectory, centralizedOutputPath, entry) -> { };
  }

  private DocumentPersistenceService fakeDocumentPersistenceService() {
    return (repositoryPath, outputDirectory, centralizedOutputPath, documentName, content,
        overwriteExisting) -> Path.of(repositoryPath, outputDirectory, documentName);
  }

  private MarkdownDocumentService fakeMarkdownDocumentService() {
    return new MarkdownDocumentService() {
      @Override
      public String buildDocumentName(String repositoryName) {
        return "repo.md";
      }

      @Override
      public int resolveDocumentVersion(Optional<RepositoryDocumentSnapshot> previousDocument) {
        return 1;
      }

      @Override
      public String createMarkdown(PromptExecutionRequest request, String generatedContent,
          String commitHash, int version, Instant generatedAt) {
        return generatedContent;
      }
    };
  }

  private KnowledgeProjectionService knowledgeProjectionService() {
    return new DefaultKnowledgeProjectionService();
  }

  private KnowledgeBaseAggregator knowledgeBaseAggregator() {
    return new DefaultKnowledgeBaseAggregator();
  }

  private DocumentContextFactory documentContextFactory() {
    return new DocumentContextFactory(templateRepository(), new AudienceProfileResolver(),
        new CommunicationProfileResolver(), new DefaultTemplateRenderer(
            new KnowledgeSelectionEngine(), new KnowledgeAdaptationEngine(),
            new SectionPlanningEngine(), new TraceabilityAssembler()));
  }

  private DocumentationContextRegistry registryReturning(
      Optional<DocumentationGenerationContext> result) {
    return new DocumentationContextRegistry() {
      @Override
      public void register(DocumentationGenerationContext ignored) {
      }

      @Override
      public Optional<DocumentationGenerationContext> findByScanId(String scanId) {
        return result;
      }
    };
  }

  private DocumentationGenerationContext context() {
    ScanExecutionMetadata metadata = new ScanExecutionMetadata("fp", false, "", "",
        "FULL", "2.0", "2.2", 10L, 1L, 0L, 1L, 1L, 1L, 1L, 1L, 5L, 100L);
    CodeScanSummary summary = new CodeScanSummary("scan-1", "fp", Instant.now(), 1,
        1, 0, 0, 1, Map.of("java", 1L), Map.of("java", 1L), List.of(), List.of(),
        List.of(), List.of(), metadata);
    return new DocumentationGenerationContext("scan-1", "sample-repo", "/tmp/repo",
        "BancoPichinchaEC", "taa-serviciosadministrativos", 42L, summary);
  }

  private TemplateRepository templateRepository() {
    DocumentTemplate template = new DocumentTemplate("error-manual",
        "Error Manual", new TemplateVersion("1.0.0"), List.of(),
        TemplateMetadata.internal());
    return new TemplateRepository() {
      @Override
      public Optional<TemplateResolutionResult> resolveActive(String templateId) {
        return Optional.of(TemplateResolutionResult.resolved(template, "catalog"));
      }

      @Override
      public Optional<TemplateResolutionResult> resolveVersion(String templateId,
          TemplateVersion version) {
        return Optional.empty();
      }

      @Override
      public List<TemplateResolutionResult> searchByName(String templateName) {
        return List.of();
      }
    };
  }

  private TemplateRegistryService templateRegistryService() {
    return new TemplateRegistryService(
        new CompositeTemplateSource(List.<ExternalTemplateSource>of()),
        new TemplateValidator(), new TemplateCatalog()) {
      @Override
      public TemplateRegistrationResult registerAvailableTemplates() {
        return new TemplateRegistrationResult(0, 0, 0, 0, List.of(), Map.of());
      }
    };
  }
}
