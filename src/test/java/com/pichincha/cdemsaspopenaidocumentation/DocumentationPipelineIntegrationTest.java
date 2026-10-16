package com.pichincha.cdemsaspopenaidocumentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pichincha.cdemsaspopenaidocumentation.copilot.PromptCommandExecutor;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.CopilotExecutionOutput;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.PromptExecutionStatus;
import com.pichincha.cdemsaspopenaidocumentation.domain.DocumentGenerationResult;
import com.pichincha.cdemsaspopenaidocumentation.domain.RepositoryCloneRequest;
import com.pichincha.cdemsaspopenaidocumentation.domain.RepositoryCloneResponse;
import com.pichincha.cdemsaspopenaidocumentation.domain.enums.DocumentGenerationStatusEnum;
import com.pichincha.cdemsaspopenaidocumentation.domain.enums.ScanScopeEnum;
import com.pichincha.cdemsaspopenaidocumentation.service.RepositoryProcessingOrchestrator;
import com.pichincha.cdemsaspopenaidocumentation.support.GitTestRepository;
import com.pichincha.cdemsaspopenaidocumentation.support.TemporalTestServerSupport;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Runs the real pipeline (Temporal workflow, git checkout of the pull request merge ref,
 * incremental scan, H2 persistence, templates, prompts and skills of the repository and file
 * output) with only the Copilot CLI replaced by a deterministic stub that records every prompt.
 */
@SpringBootTest
class DocumentationPipelineIntegrationTest extends TemporalTestServerSupport {

  private static final Path WORKSPACE = createWorkspace();
  private static final Path REPOSITORY_OUTPUT = WORKSPACE.resolve("output/org/project/repo");
  private static final Path CLONES = WORKSPACE.resolve("clones");
  private static final String EDITORIAL_PROMPT_TITLE = "# Agente Editorial";

  @Autowired
  private RepositoryProcessingOrchestrator orchestrator;

  @Autowired
  private StubPromptCommandExecutor copilot;

  @DynamicPropertySource
  static void workspaceProperties(DynamicPropertyRegistry registry) {
    registry.add("app.git.base-url", () -> WORKSPACE.resolve("remotes").toUri().toString());
    registry.add("app.git.clone-directory", CLONES::toString);
    registry.add("documentation.output-root", () -> WORKSPACE.resolve("output").toString());
  }

  @Test
  void pipelineShouldEvolveDocumentationAcrossPullRequests() throws IOException {
    GitTestRepository remote = GitTestRepository.init(
        WORKSPACE.resolve("remotes/org/project/repo"));
    initialSources(remote);
    assertFirstExecution(process(remote, 7L));
    remote.write("src/main/resources/application.yml",
        "spring:\n  profiles: dev\nclient:\n  timeout: 9000\n");
    assertSecondExecution(process(remote, 8L));

    RepositoryCloneResponse third = orchestrator.process(request(8L), true);

    assertEquals(0, third.scan().changedFiles());
    assertEquals(List.of(DocumentGenerationStatusEnum.UP_TO_DATE,
        DocumentGenerationStatusEnum.UP_TO_DATE, DocumentGenerationStatusEnum.NO_CHANGES),
        statuses(third));
    assertEquals(2, historicalDocuments().size());
    assertCopilotWorkedOnlyWithEvidence();
  }

  private void assertCopilotWorkedOnlyWithEvidence() {
    assertFalse(copilot.prompts.isEmpty());
    assertTrue(copilot.clonedFilesSeen.stream().allMatch(count -> count == 0));
    assertTrue(copilot.prompts.stream().noneMatch(prompt -> prompt.contains(CLONES.toString())));
    assertTrue(copilot.renderPrompts().anyMatch(prompt -> prompt.contains("(FUNCTIONAL_USERS)")
        && prompt.contains("### Skill: errores-para-negocio")));
    assertTrue(copilot.renderPrompts().allMatch(prompt -> prompt.contains(
        "### Skill: lenguaje-claro") && !prompt.contains("{{")));
    assertTrue(copilot.prompts.stream().filter(prompt -> prompt.startsWith(EDITORIAL_PROMPT_TITLE))
        .allMatch(prompt -> prompt.contains("<seccion id=") && !prompt.contains("{{")));
  }

  private void assertFirstExecution(RepositoryCloneResponse response) throws IOException {
    assertEquals(ScanScopeEnum.FULL, response.scan().scope());
    assertEquals("NO_BASELINE", response.scan().scopeReason());
    assertTrue(response.scan().pullRequestResolved());
    assertEquals(List.of(DocumentGenerationStatusEnum.GENERATED,
        DocumentGenerationStatusEnum.GENERATED, DocumentGenerationStatusEnum.GENERATED),
        statuses(response));
    String functionalGuide = Files.readString(
        REPOSITORY_OUTPUT.resolve("error-manual-funcional.md"));
    assertTrue(functionalGuide.contains("## Glosario\n\nTexto revisado para glossary"));
    assertFalse(Files.readString(historicalDocuments().getFirst()).contains("Texto revisado"));
    assertTrue(Files.readString(REPOSITORY_OUTPUT.resolve("error-manual.md"))
        .contains("document-version: 1"));
  }

  private void assertSecondExecution(RepositoryCloneResponse response) throws IOException {
    assertEquals(ScanScopeEnum.INCREMENTAL, response.scan().scope());
    assertEquals(1, response.scan().changedFiles());
    assertEquals(1, response.scan().filesScanned());
    DocumentGenerationResult errorManual = response.documents().getFirst();
    assertEquals(2, errorManual.documentVersion());
    String changelog = Files.readString(REPOSITORY_OUTPUT.resolve("error-manual.changelog.md"));
    assertTrue(changelog.indexOf("## Version 2") < changelog.indexOf("## Version 1"));
    assertTrue(changelog.contains("INCREMENTAL (1 archivos cambiados)"));
  }

  private RepositoryCloneResponse process(GitTestRepository remote, long pullRequestId)
      throws IOException {
    remote.commit("change for pull request " + pullRequestId);
    remote.createPullRequestRef(pullRequestId);
    return orchestrator.process(request(pullRequestId), true);
  }

  private void initialSources(GitTestRepository remote) throws IOException {
    remote.write("src/main/java/com/example/PaymentController.java",
            "@RestController\nclass PaymentController {\n  @GetMapping(\"/payments\")\n"
                + "  void list() { throw new IllegalStateException(\"down\"); }\n}\n")
        .write("src/main/java/com/example/LedgerClient.java",
            "@FeignClient(name = \"ledger\", url = \"https://ledger.internal\")\n"
                + "interface LedgerClient {}\n")
        .write("src/main/resources/application.yml",
            "spring:\n  profiles: dev\nclient:\n  timeout: 5000\n");
  }

  private RepositoryCloneRequest request(long pullRequestId) {
    return new RepositoryCloneRequest("org", "project", "repo", pullRequestId, List.of(),
        false);
  }

  private List<DocumentGenerationStatusEnum> statuses(RepositoryCloneResponse response) {
    return response.documents().stream().map(DocumentGenerationResult::status).toList();
  }

  private List<Path> historicalDocuments() throws IOException {
    try (Stream<Path> files = Files.list(REPOSITORY_OUTPUT.resolve(
        "historial/pr-change-history"))) {
      return files.toList();
    }
  }

  private static Path createWorkspace() {
    try {
      return Files.createTempDirectory("documentation-pipeline-it");
    } catch (IOException exception) {
      throw new UncheckedIOException(exception);
    }
  }

  @TestConfiguration
  static class StubCopilotConfiguration {

    @Bean
    @Primary
    StubPromptCommandExecutor stubPromptCommandExecutor() {
      return new StubPromptCommandExecutor();
    }
  }

  static final class StubPromptCommandExecutor implements PromptCommandExecutor {

    private static final Pattern SPECIALIST_EVIDENCE = Pattern.compile("\\[id:(EV-[0-9a-f]+)]");
    private static final Pattern CONSOLIDATED_EVIDENCE =
        Pattern.compile("evidencia: (EV-[0-9a-f]+)");
    private static final Pattern RENDER_SECTION =
        Pattern.compile("(?m)^- \\[([^\\]]+)] .* \\| objetivo: ");
    private static final String RENDER_PROMPT_TITLE = "# Agente de Renderizado de Plantilla";
    private static final Pattern EDITORIAL_SECTION = Pattern.compile("<seccion id=\"([^\"]+)\"");
    private final ObjectMapper mapper = new ObjectMapper();
    private final List<String> prompts = new CopyOnWriteArrayList<>();
    private final List<Long> clonedFilesSeen = new CopyOnWriteArrayList<>();

    @Override
    public CopilotExecutionOutput execute(String prompt, String model) {
      prompts.add(prompt);
      clonedFilesSeen.add(clonedFiles());
      return new CopilotExecutionOutput(respond(prompt), "", PromptExecutionStatus.SUCCESS);
    }

    private Stream<String> renderPrompts() {
      return prompts.stream().filter(prompt -> prompt.startsWith(RENDER_PROMPT_TITLE));
    }

    private long clonedFiles() {
      if (!Files.exists(CLONES)) {
        return 0;
      }
      try (Stream<Path> files = Files.walk(CLONES)) {
        return files.filter(Files::isRegularFile).count();
      } catch (IOException exception) {
        throw new UncheckedIOException(exception);
      }
    }

    private String respond(String prompt) {
      if (prompt.startsWith(RENDER_PROMPT_TITLE)) {
        return toJson(sections(RENDER_SECTION.matcher(prompt), "Contenido generado para "));
      }
      if (prompt.startsWith(EDITORIAL_PROMPT_TITLE)) {
        return toJson(sections(EDITORIAL_SECTION.matcher(prompt), "Texto revisado para "));
      }
      Pattern evidencePattern = prompt.startsWith("# Agente de Consolidación")
          ? CONSOLIDATED_EVIDENCE : SPECIALIST_EVIDENCE;
      return toJson(Map.of("findings", findings(evidencePattern.matcher(prompt))));
    }

    private List<Map<String, Object>> findings(Matcher evidence) {
      if (!evidence.find()) {
        return List.of();
      }
      return List.of(Map.of("findingId", "finding-1", "title", "Hallazgo",
          "businessSummary", "impacto", "technicalSummary", "detalle", "severity", "medium",
          "evidenceIds", List.of(evidence.group(1))));
    }

    private Map<String, String> sections(Matcher matcher, String bodyPrefix) {
      Map<String, String> sections = new LinkedHashMap<>();
      while (matcher.find()) {
        sections.put(matcher.group(1), bodyPrefix + matcher.group(1));
      }
      return sections;
    }

    private String toJson(Object value) {
      try {
        return mapper.writeValueAsString(value);
      } catch (JsonProcessingException exception) {
        throw new IllegalStateException(exception);
      }
    }
  }
}
