package com.pichincha.cdemsaspopenaidocumentation.copilot.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pichincha.cdemsaspopenaidocumentation.copilot.ChangelogService;
import com.pichincha.cdemsaspopenaidocumentation.copilot.DocumentStateRepository;
import com.pichincha.cdemsaspopenaidocumentation.copilot.EditorialReviewAgent;
import com.pichincha.cdemsaspopenaidocumentation.copilot.TemplateRenderAgent;
import com.pichincha.cdemsaspopenaidocumentation.copilot.config.DocumentationProperties;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.ChangelogEntry;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.ConsolidatedKnowledge;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentContext;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentStateSnapshot;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.EditorialReviewRequest;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.OutputContract;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.SectionState;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.TemplateRenderRequest;
import com.pichincha.cdemsaspopenaidocumentation.copilot.strategy.EvolutionaryDocumentStrategy;
import com.pichincha.cdemsaspopenaidocumentation.copilot.strategy.HistoricalDocumentStrategy;
import com.pichincha.cdemsaspopenaidocumentation.documentation.communication.domain.PresentationMode;
import com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.domain.RenderingPlan;
import com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.domain.SectionContext;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.DocumentTemplate;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateGuidance;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateMetadata;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateVersion;
import com.pichincha.cdemsaspopenaidocumentation.domain.CodeSignalEvidence;
import com.pichincha.cdemsaspopenaidocumentation.domain.DocumentGenerationResult;
import com.pichincha.cdemsaspopenaidocumentation.domain.enums.DocumentGenerationStatusEnum;
import com.pichincha.cdemsaspopenaidocumentation.domain.enums.DocumentLifecycleEnum;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.EvidenceRef;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MultiAgentDocumentationOrchestratorTest {

  private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-10-16T10:15:30Z"),
      ZoneOffset.UTC);

  @TempDir
  private Path outputRoot;

  private final InMemoryDocumentStateRepository stateRepository =
      new InMemoryDocumentStateRepository();
  private final InMemoryChangelogService changelogService = new InMemoryChangelogService();
  private final ScriptedRenderAgent renderAgent = new ScriptedRenderAgent();
  private final PrefixingEditorialAgent editorialAgent = new PrefixingEditorialAgent();
  private MultiAgentDocumentationOrchestrator orchestrator;

  @BeforeEach
  void setUp() {
    IncrementalSectionPlanner planner = new IncrementalSectionPlanner();
    DocumentPublisher publisher = new DocumentPublisher(changelogService,
        new FileSystemDocumentPersistenceService(new DocumentationProperties(outputRoot)),
        new DocumentAssembler(), CLOCK);
    orchestrator = new MultiAgentDocumentationOrchestrator(List.of(
        new EvolutionaryDocumentStrategy(stateRepository, planner, publisher, changelogService),
        new HistoricalDocumentStrategy(publisher)),
        new SpecialistKnowledgeCollector(new DefaultEvidencePartitionService(), List.of()),
        request -> new ConsolidatedKnowledge(List.of(), List.of(), List.of("check timeouts")),
        renderAgent, new EditorialReviewer(editorialAgent), CLOCK);
  }

  @Test
  void firstEvolutionaryExecutionShouldRenderEverySectionAndWriteChangelog() throws IOException {
    DocumentGenerationResult result = orchestrator.generate(
        context(DocumentLifecycleEnum.EVOLUTIONARY, "EV-1", "EV-2"));

    assertEquals(DocumentGenerationStatusEnum.GENERATED, result.status());
    assertEquals(1, result.documentVersion());
    assertEquals(List.of("Alcance", "Catalogo"), result.updatedSections());
    String document = Files.readString(outputRoot.resolve("org/project/repo/error-manual.md"));
    assertTrue(document.contains("document-version: 1"));
    assertTrue(document.contains("## Catalogo\n\nbody-catalog-1"));
    assertEquals(List.of("org/project/repo/error-manual"), changelogService.writtenChangelogs);
    assertEquals(List.of("check timeouts"), changelogService.entries.getFirst().warnings());
  }

  @Test
  void evolutionaryExecutionWithUnchangedInputsShouldBeUpToDateWithoutCallingTheLlm() {
    orchestrator.generate(context(DocumentLifecycleEnum.EVOLUTIONARY, "EV-1", "EV-2"));

    DocumentGenerationResult result = orchestrator.generate(
        context(DocumentLifecycleEnum.EVOLUTIONARY, "EV-1", "EV-2"));

    assertEquals(DocumentGenerationStatusEnum.UP_TO_DATE, result.status());
    assertEquals(1, renderAgent.invocations);
    assertEquals(1, changelogService.entries.size());
  }

  @Test
  void evolutionaryExecutionShouldRegenerateOnlyChangedSectionsAndKeepTheRest()
      throws IOException {
    orchestrator.generate(context(DocumentLifecycleEnum.EVOLUTIONARY, "EV-1", "EV-2"));

    DocumentGenerationResult result = orchestrator.generate(
        context(DocumentLifecycleEnum.EVOLUTIONARY, "EV-1", "EV-3"));

    assertEquals(2, result.documentVersion());
    assertEquals(List.of("Catalogo"), result.updatedSections());
    assertEquals(List.of("catalog"), renderAgent.lastRequestedSections);
    String document = Files.readString(outputRoot.resolve("org/project/repo/error-manual.md"));
    assertTrue(document.contains("body-scope-1"));
    assertTrue(document.contains("body-catalog-2"));
  }

  @Test
  void sectionOmittedByTheRenderAgentShouldBeRetriedInsteadOfBeingLost() {
    renderAgent.omittedSections.add("catalog");
    DocumentGenerationResult first = orchestrator.generate(
        context(DocumentLifecycleEnum.EVOLUTIONARY, "EV-1", "EV-2"));
    renderAgent.omittedSections.clear();

    DocumentGenerationResult second = orchestrator.generate(
        context(DocumentLifecycleEnum.EVOLUTIONARY, "EV-1", "EV-2"));

    assertEquals(List.of("Alcance"), first.updatedSections());
    assertEquals(List.of("Catalogo"), second.updatedSections());
    assertEquals(DocumentGenerationStatusEnum.UP_TO_DATE, orchestrator.generate(
        context(DocumentLifecycleEnum.EVOLUTIONARY, "EV-1", "EV-2")).status());
  }

  @Test
  void historicalExecutionShouldWriteADatedDocumentWithEverySection() throws IOException {
    DocumentGenerationResult result = orchestrator.generate(
        context(DocumentLifecycleEnum.HISTORICAL, "EV-1", "EV-2"));

    assertEquals(DocumentGenerationStatusEnum.GENERATED, result.status());
    Path document = outputRoot.resolve(
        "org/project/repo/historial/error-manual/20261016-101530-pr-7.md");
    assertTrue(Files.readString(document).contains("# Error Manual - PR #7 - 2026-10-16"));
    assertTrue(changelogService.writtenChangelogs.isEmpty());
  }

  @Test
  void historicalExecutionWithoutChangedEvidenceShouldReportNoChanges() {
    DocumentGenerationResult result = orchestrator.generate(
        context(DocumentLifecycleEnum.HISTORICAL));

    assertEquals(DocumentGenerationStatusEnum.NO_CHANGES, result.status());
    assertEquals(0, renderAgent.invocations);
  }

  @Test
  void editorialReviewShouldPublishTheRevisedSectionsWhenTheTemplateEnablesIt()
      throws IOException {
    orchestrator.generate(context(DocumentLifecycleEnum.EVOLUTIONARY, true, "EV-1", "EV-2"));

    String document = Files.readString(outputRoot.resolve("org/project/repo/error-manual.md"));
    assertTrue(document.contains("## Catalogo\n\nrevised body-catalog-1"));
    assertEquals(1, editorialAgent.invocations);
  }

  @Test
  void editorialReviewShouldNotRunWhenTheTemplateDoesNotEnableIt() {
    orchestrator.generate(context(DocumentLifecycleEnum.EVOLUTIONARY, "EV-1", "EV-2"));

    assertEquals(0, editorialAgent.invocations);
  }

  private DocumentContext context(DocumentLifecycleEnum lifecycle, String... evidenceIds) {
    return context(lifecycle, false, evidenceIds);
  }

  private DocumentContext context(DocumentLifecycleEnum lifecycle, boolean editorialReview,
      String... evidenceIds) {
    List<CodeSignalEvidence> evidences = Arrays.stream(evidenceIds)
        .map(id -> TestDocumentationContextFactory.evidence(id, "API")).toList();
    List<SectionContext> sections = List.of(
        section("scope", "Alcance", evidenceIds.length > 0 ? evidenceIds[0] : "none"),
        section("catalog", "Catalogo", evidenceIds.length > 1 ? evidenceIds[1] : "none"));
    DocumentTemplate template = new DocumentTemplate("error-manual", "Error Manual",
        new TemplateVersion("1.0.0"), List.of(), TemplateMetadata.internal(),
        new TemplateGuidance(lifecycle, List.of(), null, List.of(), editorialReview));
    RenderingPlan plan = new RenderingPlan(sections, List.of(), List.of(), List.of());
    return new DocumentContext(TestDocumentationContextFactory.context(evidences), template,
        new OutputContract("es", "No evidenciado en el escaneo.", List.of()), plan,
        List.of(), "sig");
  }

  private SectionContext section(String id, String title, String evidenceId) {
    return new SectionContext(id, title, "objective", List.of(), true, 1, "intent",
        PresentationMode.SUMMARY_FIRST, List.of(),
        List.of(new EvidenceRef(evidenceId, "A.java", "rule", "api", 1.0)), List.of(), "");
  }

  private static final class PrefixingEditorialAgent implements EditorialReviewAgent {

    private int invocations;

    @Override
    public Map<String, String> review(EditorialReviewRequest request) {
      invocations++;
      Map<String, String> revisions = new LinkedHashMap<>();
      request.drafts().forEach((sectionId, draft) -> revisions.put(sectionId,
          "revised " + draft));
      return revisions;
    }

    @Override
    public String reviewSignature() {
      return "editor";
    }
  }

  private static final class ScriptedRenderAgent implements TemplateRenderAgent {

    private final Set<String> omittedSections = new HashSet<>();
    private List<String> lastRequestedSections = List.of();
    private int invocations;

    @Override
    public Map<String, String> render(TemplateRenderRequest request) {
      invocations++;
      lastRequestedSections = request.impactedSections().stream()
          .map(SectionContext::sectionId).toList();
      Map<String, String> bodies = new LinkedHashMap<>();
      lastRequestedSections.stream().filter(id -> !omittedSections.contains(id))
          .forEach(id -> bodies.put(id, "body-" + id + "-" + invocations));
      return bodies;
    }

    @Override
    public String renderSignature() {
      return "render";
    }
  }

  private static final class InMemoryDocumentStateRepository implements DocumentStateRepository {

    private final Map<String, Map<String, SectionState>> states = new HashMap<>();

    @Override
    public DocumentStateSnapshot load(String documentKey) {
      return new DocumentStateSnapshot(states.getOrDefault(documentKey, Map.of()));
    }

    @Override
    public void save(String documentKey, Map<String, SectionState> sectionsById) {
      states.computeIfAbsent(documentKey, key -> new HashMap<>()).putAll(sectionsById);
    }
  }

  private static final class InMemoryChangelogService implements ChangelogService {

    private final List<ChangelogEntry> entries = new ArrayList<>();
    private final List<String> writtenChangelogs = new ArrayList<>();

    @Override
    public int nextVersion(String documentKey) {
      return (int) entries.stream().filter(entry -> entry.documentKey().equals(documentKey))
          .count() + 1;
    }

    @Override
    public void record(ChangelogEntry entry) {
      entries.add(entry);
    }

    @Override
    public void writeChangelog(String documentKey, Path changelogPath, String documentTitle) {
      writtenChangelogs.add(documentKey);
    }
  }
}
