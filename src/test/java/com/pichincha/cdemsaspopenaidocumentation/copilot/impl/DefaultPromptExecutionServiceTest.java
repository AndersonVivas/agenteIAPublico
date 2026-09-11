package com.pichincha.cdemsaspopenaidocumentation.copilot.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pichincha.cdemsaspopenaidocumentation.copilot.DocumentPersistenceService;
import com.pichincha.cdemsaspopenaidocumentation.copilot.MarkdownDocumentService;
import com.pichincha.cdemsaspopenaidocumentation.copilot.PromptCommandExecutor;
import com.pichincha.cdemsaspopenaidocumentation.copilot.RepositoryCommitResolver;
import com.pichincha.cdemsaspopenaidocumentation.copilot.RepositoryDocumentResolver;
import com.pichincha.cdemsaspopenaidocumentation.copilot.config.DocumentationProperties;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.CopilotExecutionOutput;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentTemplateType;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.PromptExecutionRequest;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.PromptExecutionResult;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.PromptExecutionStatus;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.RepositoryDocumentSnapshot;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class DefaultPromptExecutionServiceTest {

  @Test
  void shouldExecuteInEvolutionaryModeAndPersistDocument() {
    AtomicReference<String> capturedPrompt = new AtomicReference<>("");
    RepositoryDocumentResolver resolver = (repo, outDir, name) -> Optional.of(
        new RepositoryDocumentSnapshot(Path.of(repo, outDir, name), "old doc", 2));
    PromptCommandExecutor commandExecutor = (prompt, path) -> {
      capturedPrompt.set(prompt);
      return new CopilotExecutionOutput("new content", "", PromptExecutionStatus.SUCCESS);
    };
    MarkdownDocumentService markdownService = new TestMarkdownService();
    DocumentPersistenceService persistence =
        (repo, outDir, centralizedPath, name, content, overwrite) ->
            Path.of(repo, outDir, name);
    DocumentationProperties properties = new DocumentationProperties("docs", "", true, true);
    RepositoryCommitResolver commitResolver = path -> Optional.of("abc123");

    DefaultPromptExecutionService service = new DefaultPromptExecutionService(
        resolver, commandExecutor, markdownService, persistence, properties, commitResolver);
    PromptExecutionResult result = service.execute(request());

    assertEquals(PromptExecutionStatus.SUCCESS, result.executionStatus());
    assertEquals("repository-agent.md", result.documentName());
    assertTrue(result.documentPath().endsWith("docs/repository-agent.md"));
    assertTrue(capturedPrompt.get().contains("## Existing Documentation"));
    assertTrue(capturedPrompt.get().contains("old doc"));
  }

  @Test
  void shouldRemoveProcessNarrationFromSuccessfulOutput() {
    DefaultPromptExecutionService service = serviceForOutput(rawProcessNarrationOutput(),
      false);
    PromptExecutionResult result = service.execute(request());

    assertEquals(PromptExecutionStatus.SUCCESS, result.executionStatus());
    assertTrue(result.generatedContent().startsWith("## Error Catalog"));
    assertTrue(!result.generatedContent().contains("I'll analyze the evidence"));
    assertTrue(!result.generatedContent().contains("Voy a analizar la evidencia"));
    assertTrue(!result.generatedContent().contains("● List directory ."));
    assertTrue(!result.generatedContent().contains("Ahora voy a"));
    assertTrue(!result.generatedContent().contains("Perfect. I've successfully generated"));
  }

  @Test
  void shouldReturnFallbackWhenOutputContainsOnlyToolTraceNoise() {
    DefaultPromptExecutionService service = serviceForOutput(rawToolTraceOutput(), false);
    PromptExecutionResult result = service.execute(request());

    assertEquals(PromptExecutionStatus.SUCCESS, result.executionStatus());
    assertTrue(result.generatedContent().contains("No se pudo generar contenido util"));
    assertTrue(!result.generatedContent().contains("Multiple validation errors"));
    assertTrue(!result.generatedContent().contains("Permission denied"));
  }

  @Test
  void shouldRetryWithDirectResponseWhenFirstOutputIsToolTraceNoise() {
    AtomicInteger invocationCount = new AtomicInteger();
    AtomicReference<String> retryPrompt = new AtomicReference<>("");
    PromptCommandExecutor commandExecutor = (prompt, path) -> {
      int currentInvocation = invocationCount.incrementAndGet();
      if (currentInvocation == 1) {
        return new CopilotExecutionOutput(rawToolTraceOutput(), "",
            PromptExecutionStatus.SUCCESS);
      }
      retryPrompt.set(prompt);
      return new CopilotExecutionOutput("## Catalogo de Errores\n- ERR-001: Timeout",
          "", PromptExecutionStatus.SUCCESS);
    };
    DefaultPromptExecutionService service = serviceForExecutor(commandExecutor, false);

    PromptExecutionResult result = service.execute(request());

    assertEquals(2, invocationCount.get());
    assertTrue(retryPrompt.get().contains("## Respuesta Obligatoria"));
    assertTrue(result.generatedContent().startsWith("## Catalogo de Errores"));
  }

  @Test
  void shouldTruncateTrailingToolTraceAfterValidManualContent() {
    DefaultPromptExecutionService service = serviceForOutput(validManualWithTrailingNoise(),
        false);

    PromptExecutionResult result = service.execute(request());

    assertEquals(PromptExecutionStatus.SUCCESS, result.executionStatus());
    assertTrue(result.generatedContent().contains("## Catalogo de Errores"));
    assertTrue(!result.generatedContent().contains("Verify current working directory"));
    assertTrue(!result.generatedContent().contains("Continuando con la generacion"));
    assertTrue(!result.generatedContent().contains("Multiple validation errors"));
    assertTrue(!result.generatedContent().contains("pwd"));
  }

  private DefaultPromptExecutionService serviceForOutput(String output,
      boolean evolutionaryMode) {
    PromptCommandExecutor commandExecutor = (prompt, path) ->
      new CopilotExecutionOutput(output, "", PromptExecutionStatus.SUCCESS);
    return serviceForExecutor(commandExecutor, evolutionaryMode);
    }

    private DefaultPromptExecutionService serviceForExecutor(
      PromptCommandExecutor commandExecutor, boolean evolutionaryMode) {
    RepositoryDocumentResolver resolver = (repo, outDir, name) -> Optional.empty();
    MarkdownDocumentService markdownService = new TestMarkdownService();
    DocumentPersistenceService persistence =
        (repo, outDir, centralizedPath, name, content, overwrite) ->
            Path.of(repo, outDir, name);
    DocumentationProperties properties = new DocumentationProperties("docs", "", true,
        evolutionaryMode);
    RepositoryCommitResolver commitResolver = path -> Optional.of("abc123");
    return new DefaultPromptExecutionService(resolver, commandExecutor, markdownService,
        persistence, properties, commitResolver);
  }

  private String rawProcessNarrationOutput() {
    return "I'll analyze the evidence\n"
      + "Voy a analizar la evidencia\n"
      + "● List directory .\n"
      + "Ahora voy a crear la documentacion\n"
        + "List directory .\n"
        + "## Error Catalog\n"
        + "- ERR-001: Timeout\n"
        + "Perfect. I've successfully generated";
  }

        private String rawToolTraceOutput() {
          return "I'll generate a comprehensive operational Error Manual\n"
          + "✗ create create file\n"
          + "  └ Multiple validation errors:\n"
          + "    - \"path\": Required\n"
          + "    - \"file_text\": Required\n"
          + "✗ Read sad-msa-bs-remittance\n"
          + "  └ Permission denied and could not request permission from user\n"
          + "Try 'copilot --help' for more information.";
        }

          private String validManualWithTrailingNoise() {
            return "# Manual Operacional de Errores\n"
            + "## Catalogo de Errores\n"
            + "- ERR-101: Timeout en dependencia critica\n"
            + "## Playbooks de Recuperacion\n"
            + "1. Validar salud de la dependencia\n"
            + "● Verify current working directory (shell)\n"
            + "  │ pwd\n"
            + "  └ 2 lines...\n"
            + "Continuando con la generacion del manual operacional...\n"
            + "✗ create create file\n"
            + "  └ Multiple validation errors:\n"
            + "    - \"path\": Required\n"
            + "    - \"file_text\": Required";
          }

  private PromptExecutionRequest request() {
    return new PromptExecutionRequest("prompt-1", "repository-agent", "/tmp/repo",
        DocumentTemplateType.ERROR_MANUAL, "base prompt", Map.of("commitHash", "fallback"));
  }

  private static final class TestMarkdownService implements MarkdownDocumentService {

    @Override
    public String buildDocumentName(String repositoryName) {
      return repositoryName + ".md";
    }

    @Override
    public int resolveDocumentVersion(
        Optional<RepositoryDocumentSnapshot> previousDocument) {
      return previousDocument.map(snapshot -> snapshot.version()).orElse(0) + 1;
    }

    @Override
    public String createMarkdown(PromptExecutionRequest request, String generatedContent,
        String commitHash, int version, java.time.Instant generatedAt) {
      return generatedContent + "|" + commitHash + "|" + version;
    }
  }
}
