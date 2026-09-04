package com.pichincha.cdemsaspopenaidocumentation.copilot.impl;

import com.pichincha.cdemsaspopenaidocumentation.copilot.DocumentPersistenceService;
import com.pichincha.cdemsaspopenaidocumentation.copilot.MarkdownDocumentService;
import com.pichincha.cdemsaspopenaidocumentation.copilot.PromptCommandExecutor;
import com.pichincha.cdemsaspopenaidocumentation.copilot.PromptExecutionService;
import com.pichincha.cdemsaspopenaidocumentation.copilot.RepositoryCommitResolver;
import com.pichincha.cdemsaspopenaidocumentation.copilot.RepositoryDocumentResolver;
import com.pichincha.cdemsaspopenaidocumentation.copilot.config.DocumentationProperties;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.CopilotExecutionOutput;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.PromptExecutionRequest;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.PromptExecutionResult;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.PromptExecutionStatus;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.RepositoryDocumentSnapshot;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class DefaultPromptExecutionService implements PromptExecutionService {

  private static final String PROVIDER = "github-copilot-cli";
  private static final String EMPTY_MANUAL_FALLBACK = "No se pudo generar contenido "
      + "util para el manual de errores con la evidencia disponible.";
  private static final String DIRECT_RESPONSE_RETRY_DIRECTIVE = String.join("\n",
      "## Respuesta Obligatoria",
      "Devuelve unicamente el cuerpo final del manual en Markdown.",
      "No intentes crear archivos.",
      "No intentes leer archivos.",
      "No intentes listar directorios.",
      "No ejecutes herramientas ni simules herramientas.",
      "No describas pasos de analisis.",
      "Usa solamente la evidencia ya incluida en este prompt.");
  private final RepositoryDocumentResolver repositoryDocumentResolver;
  private final PromptCommandExecutor promptCommandExecutor;
  private final MarkdownDocumentService markdownDocumentService;
  private final DocumentPersistenceService documentPersistenceService;
  private final DocumentationProperties documentationProperties;
  private final RepositoryCommitResolver repositoryCommitResolver;

  public DefaultPromptExecutionService(RepositoryDocumentResolver repositoryDocumentResolver,
      PromptCommandExecutor promptCommandExecutor,
      MarkdownDocumentService markdownDocumentService,
      DocumentPersistenceService documentPersistenceService,
      DocumentationProperties documentationProperties,
      RepositoryCommitResolver repositoryCommitResolver) {
    this.repositoryDocumentResolver = repositoryDocumentResolver;
    this.promptCommandExecutor = promptCommandExecutor;
    this.markdownDocumentService = markdownDocumentService;
    this.documentPersistenceService = documentPersistenceService;
    this.documentationProperties = documentationProperties;
    this.repositoryCommitResolver = repositoryCommitResolver;
  }

  @Override
  public PromptExecutionResult execute(PromptExecutionRequest request) {
    Instant start = Instant.now();
    String documentName = markdownDocumentService.buildDocumentName(
        request.repositoryName());
    Optional<RepositoryDocumentSnapshot> previous = resolvePreviousDocument(request,
        documentName);
    CopilotExecutionOutput output = runCopilotExecution(request, previous);
    return buildResult(request, output, previous, documentName, start);
  }

  private Optional<RepositoryDocumentSnapshot> resolvePreviousDocument(
      PromptExecutionRequest request, String documentName) {
    return repositoryDocumentResolver.resolve(request.repositoryPath(),
        documentationProperties.outputDirectory(), documentName);
  }

  private CopilotExecutionOutput runCopilotExecution(PromptExecutionRequest request,
      Optional<RepositoryDocumentSnapshot> previous) {
    String prompt = buildPromptContent(request.promptContent(), previous);
    CopilotExecutionOutput output = promptCommandExecutor.execute(prompt,
        Path.of(request.repositoryPath()));
    if (!shouldRetryWithDirectResponse(output)) {
      return output;
    }
    String retryPrompt = String.join("\n\n", prompt, DIRECT_RESPONSE_RETRY_DIRECTIVE);
    return promptCommandExecutor.execute(retryPrompt, Path.of(request.repositoryPath()));
  }

  private String buildPromptContent(String basePrompt,
      Optional<RepositoryDocumentSnapshot> previous) {
    if (!documentationProperties.evolutionaryMode() || previous.isEmpty()) {
      return basePrompt;
    }
    return String.join("\n\n", basePrompt,
        "## Existing Documentation",
        previous.get().content(),
        "## Evolution Directive",
        "Update the documentation incrementally, preserving verified history.");
  }

  private PromptExecutionResult buildResult(PromptExecutionRequest request,
      CopilotExecutionOutput output, Optional<RepositoryDocumentSnapshot> previous,
      String documentName, Instant start) {
    long executionTime = Duration.between(start, Instant.now()).toMillis();
    String contentForPersistence = contentForPersistence(output);
    Path documentPath = persistDocument(request, contentForPersistence, previous,
        documentName);
    if (output.status() == PromptExecutionStatus.SUCCESS) {
      return successfulResult(contentForPersistence, documentName, documentPath,
          executionTime);
    }
    return failedWithDocumentResult(output, documentName, documentPath, executionTime);
  }

  private String contentForPersistence(CopilotExecutionOutput output) {
    if (StringUtils.isNotBlank(output.stdout())) {
      String sanitized = sanitizeProcessNarration(output.stdout());
      return hasUsableManualContent(sanitized) ? sanitized : EMPTY_MANUAL_FALLBACK;
    }
    if (StringUtils.isNotBlank(output.stderr())) {
      return "Copilot CLI did not return stdout. Captured stderr:\n\n"
          + output.stderr();
    }
    return "Copilot CLI did not return any output.";
  }

  private boolean shouldRetryWithDirectResponse(CopilotExecutionOutput output) {
    if (output.status() != PromptExecutionStatus.SUCCESS) {
      return false;
    }
    if (StringUtils.isBlank(output.stdout())) {
      return false;
    }
    return !hasUsableManualContent(output.stdout());
  }

  private boolean hasUsableManualContent(String content) {
    String sanitized = sanitizeProcessNarration(content);
    if (StringUtils.isBlank(sanitized)) {
      return false;
    }
    return sanitized.contains("## ") || sanitized.contains("# ");
  }

  private String sanitizeProcessNarration(String content) {
    String[] lines = StringUtils.defaultString(content).split("\\R");
    StringBuilder sanitized = new StringBuilder();
    for (String line : lines) {
      if (shouldTruncateFromLine(line, sanitized.length() > 0)) {
        break;
      }
      if (isProcessNarrationLine(line)) {
        continue;
      }
      if (sanitized.length() > 0) {
        sanitized.append('\n');
      }
      sanitized.append(line);
    }
    return sanitized.toString().trim();
  }

  private boolean shouldTruncateFromLine(String line, boolean hasUsefulContent) {
    if (!hasUsefulContent) {
      return false;
    }
    String normalized = StringUtils.defaultString(line).trim().toLowerCase();
    return normalized.startsWith("verify current working directory")
        || normalized.startsWith("continuando con la generacion")
        || normalized.startsWith("continuing with")
        || normalized.startsWith("●")
        || normalized.startsWith("✗")
        || normalized.startsWith("└")
        || normalized.startsWith("│")
        || normalized.contains("create create file")
        || normalized.contains("check repository structure (shell)")
        || normalized.contains("pwd")
        || normalized.contains("multiple validation errors");
  }

  private boolean isProcessNarrationLine(String line) {
    String normalized = StringUtils.defaultString(line).trim().toLowerCase();
    if (StringUtils.isBlank(normalized)) {
      return false;
    }
    return normalized.startsWith("i'll analyze")
        || normalized.startsWith("now let me")
        || normalized.startsWith("perfect. i've successfully")
        || normalized.startsWith("voy a analizar")
        || normalized.startsWith("ahora voy a")
        || normalized.startsWith("perfecto. he generado")
        || normalized.startsWith("/ search")
        || normalized.startsWith("buscar (glob)")
        || normalized.contains("list directory")
        || normalized.contains("listar directorio")
        || normalized.contains(" lines read")
        || normalized.contains(" lineas leidas")
        || normalized.contains("create technical_error_story")
        || normalized.contains("crear technical_error_story")
        || normalized.startsWith("read readme")
        || normalized.startsWith("leer readme")
        || normalized.startsWith("read application.yml")
        || normalized.startsWith("leer application.yml")
        || normalized.startsWith("●")
        || normalized.startsWith("✗")
        || normalized.startsWith("└")
        || normalized.startsWith("│")
        || normalized.contains("multiple validation errors")
        || normalized.contains("\"path\": required")
        || normalized.contains("\"file_text\": required")
        || normalized.contains("permission denied and could not request permission")
        || normalized.contains("check repository structure (shell)")
        || normalized.contains("let me create the manual document")
        || normalized.contains("let me check the repository structure")
        || normalized.contains("try 'copilot --help' for more information");
  }

    private PromptExecutionResult failedWithDocumentResult(CopilotExecutionOutput output,
      String documentName, Path documentPath, long executionTime) {
    String failureContent = StringUtils.isBlank(output.stderr())
      ? output.stdout() : output.stderr();
    return new PromptExecutionResult(UUID.randomUUID().toString(), failureContent,
      documentPath.toString(), documentName, output.status(), executionTime,
      PROVIDER, Instant.now());
    }

  private Path persistDocument(PromptExecutionRequest request, String generatedContent,
      Optional<RepositoryDocumentSnapshot> previous, String documentName) {
    int version = markdownDocumentService.resolveDocumentVersion(previous);
    String commitHash = resolveCommitHash(request);
    String markdown = markdownDocumentService.createMarkdown(request, generatedContent,
        commitHash, version, Instant.now());
    return documentPersistenceService.save(request.repositoryPath(),
        documentationProperties.outputDirectory(), documentName, markdown,
        documentationProperties.overwriteExisting());
  }

  private String resolveCommitHash(PromptExecutionRequest request) {
    return repositoryCommitResolver.resolveCommitHash(Path.of(request.repositoryPath()))
        .orElseGet(() -> request.metadata().getOrDefault("commitHash", "unknown"));
  }

  private PromptExecutionResult successfulResult(String generatedContent,
      String documentName, Path documentPath, long executionTime) {
    return new PromptExecutionResult(UUID.randomUUID().toString(), generatedContent,
        documentPath.toString(), documentName, PromptExecutionStatus.SUCCESS,
        executionTime, PROVIDER, Instant.now());
  }
}
