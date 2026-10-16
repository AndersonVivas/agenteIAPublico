package com.pichincha.cdemsaspopenaidocumentation.workflow.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pichincha.cdemsaspopenaidocumentation.activity.GenerateDocumentationActivity;
import com.pichincha.cdemsaspopenaidocumentation.activity.ScanRepositoryActivity;
import com.pichincha.cdemsaspopenaidocumentation.domain.DocumentGenerationResult;
import com.pichincha.cdemsaspopenaidocumentation.domain.DocumentationGenerationRequest;
import com.pichincha.cdemsaspopenaidocumentation.domain.DocumentationRunRequest;
import com.pichincha.cdemsaspopenaidocumentation.domain.RepositoryCloneResponse;
import com.pichincha.cdemsaspopenaidocumentation.domain.RepositoryCoordinates;
import com.pichincha.cdemsaspopenaidocumentation.domain.ScanCommand;
import com.pichincha.cdemsaspopenaidocumentation.domain.ScanExecutionSummary;
import com.pichincha.cdemsaspopenaidocumentation.domain.enums.DocumentGenerationStatusEnum;
import com.pichincha.cdemsaspopenaidocumentation.domain.enums.DocumentLifecycleEnum;
import com.pichincha.cdemsaspopenaidocumentation.domain.enums.ScanScopeEnum;
import com.pichincha.cdemsaspopenaidocumentation.workflow.RepositoryProcessingWorkflow;
import io.temporal.client.WorkflowOptions;
import io.temporal.testing.TestWorkflowEnvironment;
import io.temporal.worker.Worker;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RepositoryProcessingWorkflowImplTest {

  private static final String TASK_QUEUE = "workflow-test";

  private final RecordingScanActivity scanActivity = new RecordingScanActivity();
  private final RecordingGenerateActivity generateActivity = new RecordingGenerateActivity();
  private TestWorkflowEnvironment environment;

  @BeforeEach
  void setUp() {
    environment = TestWorkflowEnvironment.newInstance();
    Worker worker = environment.newWorker(TASK_QUEUE);
    worker.registerWorkflowImplementationTypes(RepositoryProcessingWorkflowImpl.class);
    worker.registerActivitiesImplementations(scanActivity, generateActivity);
    environment.start();
  }

  @AfterEach
  void tearDown() {
    environment.close();
  }

  @Test
  void processShouldScanOnceAndIsolateTheFailureOfOneTemplate() {
    RepositoryCloneResponse response = workflow().process(request(true,
        List.of("error-manual", "broken-template")));

    assertEquals(1, scanActivity.commands.size());
    assertEquals(List.of("error-manual", "broken-template"), generateActivity.templateIds);
    assertTrue(generateActivity.scanIds.stream()
        .allMatch(scanId -> scanId.equals(scanActivity.commands.getFirst().scanId())));
    assertEquals(List.of(DocumentGenerationStatusEnum.GENERATED,
        DocumentGenerationStatusEnum.FAILED),
        response.documents().stream().map(DocumentGenerationResult::status).toList());
    assertTrue(response.documents().get(1).message().contains("Template unavailable"));
  }

  @Test
  void processShouldOnlyScanWhenDocumentationIsNotRequested() {
    RepositoryCloneResponse response = workflow().process(request(false, List.of()));

    assertEquals(ScanScopeEnum.INCREMENTAL, response.scan().scope());
    assertTrue(response.documents().isEmpty());
    assertTrue(generateActivity.templateIds.isEmpty());
  }

  private RepositoryProcessingWorkflow workflow() {
    return environment.getWorkflowClient().newWorkflowStub(RepositoryProcessingWorkflow.class,
        WorkflowOptions.newBuilder().setTaskQueue(TASK_QUEUE).build());
  }

  private DocumentationRunRequest request(boolean generate, List<String> templateIds) {
    return new DocumentationRunRequest(new RepositoryCoordinates("org", "project", "repo"),
        7L, templateIds, false, generate);
  }

  public static class RecordingScanActivity implements ScanRepositoryActivity {

    private final List<ScanCommand> commands = new ArrayList<>();

    @Override
    public ScanExecutionSummary scanRepository(ScanCommand command) {
      commands.add(command);
      return new ScanExecutionSummary(command.scanId(), ScanScopeEnum.INCREMENTAL,
          "BASELINE_DIFF", "refs/pull/7/merge", "abc", true, "2.3.0", 1, 1, 1, 0,
          Instant.parse("2026-10-16T10:15:30Z"));
    }
  }

  public static class RecordingGenerateActivity implements GenerateDocumentationActivity {

    private final List<String> templateIds = new ArrayList<>();
    private final List<String> scanIds = new ArrayList<>();

    @Override
    public DocumentGenerationResult generateDocumentation(
        DocumentationGenerationRequest request) {
      templateIds.add(request.templateId());
      scanIds.add(request.scanId());
      if (request.templateId().startsWith("broken")) {
        throw new IllegalStateException("Template unavailable");
      }
      return new DocumentGenerationResult(request.templateId(),
          DocumentLifecycleEnum.EVOLUTIONARY, DocumentGenerationStatusEnum.GENERATED,
          "/out/error-manual.md", 1, List.of("Alcance"), 10L, "ok");
    }
  }
}
