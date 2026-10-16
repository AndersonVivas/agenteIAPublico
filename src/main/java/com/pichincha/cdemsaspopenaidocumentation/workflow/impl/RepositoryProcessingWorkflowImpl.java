package com.pichincha.cdemsaspopenaidocumentation.workflow.impl;

import com.pichincha.cdemsaspopenaidocumentation.activity.GenerateDocumentationActivity;
import com.pichincha.cdemsaspopenaidocumentation.activity.ScanRepositoryActivity;
import com.pichincha.cdemsaspopenaidocumentation.domain.DocumentGenerationResult;
import com.pichincha.cdemsaspopenaidocumentation.domain.DocumentationGenerationRequest;
import com.pichincha.cdemsaspopenaidocumentation.domain.DocumentationRunRequest;
import com.pichincha.cdemsaspopenaidocumentation.domain.RepositoryCloneResponse;
import com.pichincha.cdemsaspopenaidocumentation.domain.ScanCommand;
import com.pichincha.cdemsaspopenaidocumentation.domain.ScanExecutionSummary;
import com.pichincha.cdemsaspopenaidocumentation.workflow.RepositoryProcessingWorkflow;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.failure.ActivityFailure;
import io.temporal.failure.ApplicationFailure;
import io.temporal.workflow.Workflow;
import java.time.Duration;
import org.slf4j.Logger;

public class RepositoryProcessingWorkflowImpl implements RepositoryProcessingWorkflow {

  private static final Logger LOGGER = Workflow.getLogger(RepositoryProcessingWorkflowImpl.class);

  private final ScanRepositoryActivity scanRepositoryActivity = Workflow.newActivityStub(
      ScanRepositoryActivity.class, scanActivityOptions());
  private final GenerateDocumentationActivity generateDocumentationActivity = Workflow
      .newActivityStub(GenerateDocumentationActivity.class, generateDocumentationOptions());

  @Override
  public RepositoryCloneResponse process(DocumentationRunRequest request) {
    ScanExecutionSummary scan = scanRepositoryActivity.scanRepository(new ScanCommand(
        Workflow.randomUUID().toString(), request.repository(), request.pullRequestId(),
        request.forceFullScan()));
    RepositoryCloneResponse response = RepositoryCloneResponse.scanned(request,
        Workflow.getInfo().getWorkflowId(), scan);
    if (!request.generateDocumentation()) {
      return response;
    }
    return response.withDocuments(request.templateIds().stream()
        .map(templateId -> generate(scan.scanId(), templateId)).toList());
  }

  private DocumentGenerationResult generate(String scanId, String templateId) {
    try {
      return generateDocumentationActivity.generateDocumentation(
          new DocumentationGenerationRequest(scanId, templateId));
    } catch (ActivityFailure failure) {
      LOGGER.warn("Documentation for template {} failed", templateId, failure);
      return DocumentGenerationResult.failed(templateId, rootMessage(failure));
    }
  }

  private static String rootMessage(Throwable failure) {
    Throwable cause = failure;
    while (cause.getCause() != null) {
      cause = cause.getCause();
    }
    return cause instanceof ApplicationFailure applicationFailure
        ? applicationFailure.getOriginalMessage() : cause.getMessage();
  }

  private static ActivityOptions scanActivityOptions() {
    return ActivityOptions.newBuilder()
        .setStartToCloseTimeout(Duration.ofMinutes(30))
        .setRetryOptions(RetryOptions.newBuilder().setMaximumAttempts(3).build())
        .build();
  }

  private static ActivityOptions generateDocumentationOptions() {
    return ActivityOptions.newBuilder()
        .setStartToCloseTimeout(Duration.ofMinutes(45))
        .setRetryOptions(RetryOptions.newBuilder().setMaximumAttempts(1).build())
        .build();
  }
}
