package com.pichincha.cdemsaspopenaidocumentation.copilot.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentTemplateType;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.PromptExecutionRequest;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.RepositoryDocumentSnapshot;
import com.pichincha.cdemsaspopenaidocumentation.copilot.strategy.ApiDocumentationDocumentTypeStrategy;
import com.pichincha.cdemsaspopenaidocumentation.copilot.strategy.ArchitectureDocumentationDocumentTypeStrategy;
import com.pichincha.cdemsaspopenaidocumentation.copilot.strategy.ErrorManualDocumentTypeStrategy;
import com.pichincha.cdemsaspopenaidocumentation.copilot.strategy.ReadmeDocumentTypeStrategy;
import com.pichincha.cdemsaspopenaidocumentation.copilot.strategy.TechnicalDocumentationDocumentTypeStrategy;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class DefaultMarkdownDocumentServiceTest {

  @Test
  void shouldBuildRepositoryFileName() {
    DefaultMarkdownDocumentService service = service();

    String documentName = service.buildDocumentName("Cash Flow Management");

    assertEquals("cash-flow-management.md", documentName);
  }

  @Test
  void shouldGenerateMarkdownWithMetadataAndVersion() {
    DefaultMarkdownDocumentService service = service();
    PromptExecutionRequest request = request();
    int version = service.resolveDocumentVersion(Optional.of(
        new RepositoryDocumentSnapshot(Path.of("docs/repository-agent.md"), "content", 3)));

    String markdown = service.createMarkdown(request, "Generated body", "abc123",
        version, Instant.parse("2026-08-03T10:15:30Z"));

    assertEquals(4, version);
    assertTrue(markdown.contains("generated-at: 2026-08-03T10:15:30Z"));
    assertTrue(markdown.contains("repository: repository-agent"));
    assertTrue(markdown.contains("commit-hash: abc123"));
    assertTrue(markdown.contains("document-type: TECHNICAL_DOCUMENTATION"));
    assertTrue(markdown.contains("document-version: 4"));
    assertTrue(markdown.contains("# Technical Documentation"));
  }

  private DefaultMarkdownDocumentService service() {
    return new DefaultMarkdownDocumentService(List.of(
        new ErrorManualDocumentTypeStrategy(),
        new ApiDocumentationDocumentTypeStrategy(),
        new TechnicalDocumentationDocumentTypeStrategy(),
        new ReadmeDocumentTypeStrategy(),
        new ArchitectureDocumentationDocumentTypeStrategy()));
  }

  private PromptExecutionRequest request() {
    return new PromptExecutionRequest("prompt-1", "repository-agent",
        "/tmp/repository-agent", DocumentTemplateType.TECHNICAL_DOCUMENTATION,
        "base prompt", Map.of("branch", "main"));
  }
}
