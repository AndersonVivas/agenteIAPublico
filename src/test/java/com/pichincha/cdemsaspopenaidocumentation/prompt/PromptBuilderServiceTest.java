package com.pichincha.cdemsaspopenaidocumentation.prompt;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pichincha.cdemsaspopenaidocumentation.domain.CodeSignalEvidence;
import com.pichincha.cdemsaspopenaidocumentation.prompt.config.PromptBuilderProperties;
import com.pichincha.cdemsaspopenaidocumentation.prompt.domain.PromptContext;
import com.pichincha.cdemsaspopenaidocumentation.prompt.domain.PromptType;
import com.pichincha.cdemsaspopenaidocumentation.prompt.domain.RepositoryMetadata;
import com.pichincha.cdemsaspopenaidocumentation.prompt.impl.DefaultPromptBuilderService;
import com.pichincha.cdemsaspopenaidocumentation.prompt.strategy.ApiDocumentationPromptTemplateStrategy;
import com.pichincha.cdemsaspopenaidocumentation.prompt.strategy.ArchitectureDocumentationPromptTemplateStrategy;
import com.pichincha.cdemsaspopenaidocumentation.prompt.strategy.ChangelogPromptTemplateStrategy;
import com.pichincha.cdemsaspopenaidocumentation.prompt.strategy.CodeDocumentationPromptTemplateStrategy;
import com.pichincha.cdemsaspopenaidocumentation.prompt.strategy.DeploymentGuidePromptTemplateStrategy;
import com.pichincha.cdemsaspopenaidocumentation.prompt.strategy.ErrorManualPromptTemplateStrategy;
import com.pichincha.cdemsaspopenaidocumentation.prompt.strategy.ReadmeGenerationPromptTemplateStrategy;
import com.pichincha.cdemsaspopenaidocumentation.prompt.strategy.TechnicalSpecificationPromptTemplateStrategy;
import com.pichincha.cdemsaspopenaidocumentation.prompt.support.ClasspathPromptTemplateLoader;
import com.pichincha.cdemsaspopenaidocumentation.prompt.support.PromptContextFormatter;
import com.pichincha.cdemsaspopenaidocumentation.prompt.support.SimplePromptTemplateRenderer;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PromptBuilderServiceTest {

  @Test
  void buildPromptShouldRenderMarkdownTemplateWithContext() {
    DefaultPromptBuilderService service = service();
    PromptContext context = context();

    String prompt = service.buildPrompt(PromptType.ERROR_MANUAL, context);

    assertTrue(prompt.contains("# SYSTEM"));
    assertTrue(prompt.contains("# CONSTRAINTS"));
    assertTrue(prompt.contains("# OBJECTIVE"));
    assertTrue(prompt.contains("# EVIDENCE"));
    assertTrue(prompt.contains("# RAG_CONTEXT"));
    assertTrue(prompt.contains("# RULES"));
    assertTrue(prompt.contains("# OUTPUT_FORMAT"));
    assertTrue(prompt.contains("sample-repo"));
    assertTrue(prompt.contains("/business/cash-flow-management/v1/remittances"));
    assertTrue(prompt.contains("No current documentation provided.") == false);
    assertTrue(prompt.contains("Never invent information."));
  }

  @Test
  void buildPromptShouldSupportAllConfiguredPromptTypes() {
    DefaultPromptBuilderService service = service();
    PromptContext context = context();

    for (PromptType type : PromptType.values()) {
      String prompt = service.buildPrompt(type, context);
      assertTrue(prompt.contains("# SYSTEM"));
      assertTrue(prompt.contains("# CONSTRAINTS"));
      assertTrue(prompt.contains("# OBJECTIVE"));
      assertTrue(prompt.contains("# EVIDENCE"));
    }
  }

  @Test
  void buildPromptForChangelogShouldIncludeSectionsAndIncidents() {
    DefaultPromptBuilderService service = service();
    String prompt = service.buildPrompt(PromptType.CHANGELOG, context());

    assertTrue(prompt.contains("Correcciones de Errores e Incidentes"));
    assertTrue(prompt.contains("INC-1: previous outage"));
    assertTrue(prompt.contains("sample-repo"));
  }

  @Test
  void buildPromptForCodeDocumentationShouldIncludeArchitectureSections() {
    DefaultPromptBuilderService service = service();
    String prompt = service.buildPrompt(PromptType.CODE_DOCUMENTATION, context());

    assertTrue(prompt.contains("Arquitectura de Clases y Paquetes"));
    assertTrue(prompt.contains("Manejo de Excepciones"));
    assertTrue(prompt.contains("sample-repo"));
  }

  @Test
  void buildPromptForTechnicalSpecificationShouldIncludeResilienceSections() {
    DefaultPromptBuilderService service = service();
    String prompt = service.buildPrompt(PromptType.TECHNICAL_SPECIFICATION, context());

    assertTrue(prompt.contains("Interfaces y Contratos"));
    assertTrue(prompt.contains("Resiliencia, Politicas de Timeout"));
    assertTrue(prompt.contains("sample-repo"));
  }

  @Test
  void buildPromptForDeploymentGuideShouldIncludeDeploymentSections() {
    DefaultPromptBuilderService service = service();
    String prompt = service.buildPrompt(PromptType.DEPLOYMENT_GUIDE, context());

    assertTrue(prompt.contains("Variables de Entorno"));
    assertTrue(prompt.contains("Procedimientos de Rollback"));
    assertTrue(prompt.contains("sample-repo"));
  }

  @Test
  void buildPromptShouldRejectUnsupportedType() {
    DefaultPromptBuilderService service = service();

    IllegalArgumentException exception =
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
            () -> service.buildPrompt(null, context()));

    assertTrue(exception.getMessage().contains("Unsupported prompt type"));
  }

  @Test
  void contextShouldNormalizeEmptyValues() {
    PromptContext context = new PromptContext(null, null, null, null, null);

    assertEquals(0, context.evidences().size());
    assertEquals("", context.ragContext());
    assertEquals("", context.currentDocumentation());
    assertEquals(0, context.historicalIncidents().size());
    assertDoesNotThrow(() -> context.repositoryMetadata());
  }

  private DefaultPromptBuilderService service() {
    return new DefaultPromptBuilderService(
        List.of(
            new ErrorManualPromptTemplateStrategy(),
            new ApiDocumentationPromptTemplateStrategy(),
            new ArchitectureDocumentationPromptTemplateStrategy(),
            new ReadmeGenerationPromptTemplateStrategy(),
            new ChangelogPromptTemplateStrategy(),
            new CodeDocumentationPromptTemplateStrategy(),
            new TechnicalSpecificationPromptTemplateStrategy(),
            new DeploymentGuidePromptTemplateStrategy()),
        new PromptBuilderProperties(null, null),
        new ClasspathPromptTemplateLoader(),
        new SimplePromptTemplateRenderer(),
        new PromptContextFormatter());
  }

  private PromptContext context() {
    RepositoryMetadata metadata = new RepositoryMetadata("sample-repo",
        "/workspace/sample-repo", "hexagonal", "spring-boot",
        Map.of("language", "java", "branch", "main"));
    CodeSignalEvidence evidence = new CodeSignalEvidence("EV-1", "SCAN-1", "API", "source",
        "java", "spring", "src/main/resources/openapi.yaml", "remittanceController",
        "remittanceController", "RULE-API-001", "OpenAPI path found",
        "/business/cash-flow-management/v1/remittances", "high", "rest", List.of("api"),
        "Endpoint detected in OpenAPI", "Document the endpoint contract", 4, 4, 4,
        List.of("/business/cash-flow-management/v1/remittances"), 0.88,
        List.of("spring", "api"));
    return new PromptContext(metadata, List.of(evidence), "Use repository evidence only.",
        "Existing doc summary.", List.of("INC-1: previous outage"));
  }
}