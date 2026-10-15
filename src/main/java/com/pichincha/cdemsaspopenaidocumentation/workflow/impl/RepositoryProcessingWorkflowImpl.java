package com.pichincha.cdemsaspopenaidocumentation.workflow.impl;

import com.pichincha.cdemsaspopenaidocumentation.activity.CloneRepositoryActivity;
import com.pichincha.cdemsaspopenaidocumentation.activity.GenerateDocumentationActivity;
import com.pichincha.cdemsaspopenaidocumentation.activity.ScanRepositoryActivity;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentationGenerationContext;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.PromptExecutionResult;
import com.pichincha.cdemsaspopenaidocumentation.domain.CodeScanSummary;
import com.pichincha.cdemsaspopenaidocumentation.domain.RepositoryCloneRequest;
import com.pichincha.cdemsaspopenaidocumentation.domain.RepositoryCloneResponse;
import com.pichincha.cdemsaspopenaidocumentation.domain.RepositoryCloneResult;
import io.temporal.activity.ActivityOptions;
import io.temporal.common.RetryOptions;
import io.temporal.workflow.Workflow;
import java.time.Duration;

public class RepositoryProcessingWorkflowImpl implements
    com.pichincha.cdemsaspopenaidocumentation.workflow.RepositoryProcessingWorkflow {

  private final CloneRepositoryActivity cloneRepositoryActivity = Workflow.newActivityStub(
      CloneRepositoryActivity.class, cloneActivityOptions());
  private final ScanRepositoryActivity scanRepositoryActivity = Workflow.newActivityStub(
      ScanRepositoryActivity.class, scanActivityOptions());
  private final GenerateDocumentationActivity generateDocumentationActivity = Workflow
      .newActivityStub(GenerateDocumentationActivity.class, generateDocumentationActivityOptions());

  @Override
  public RepositoryCloneResponse process(RepositoryCloneRequest request,
      boolean generateDocumentation, String templateId) {
    RepositoryCloneResult cloneResult = cloneRepositoryActivity.cloneRepository(request);
    CodeScanSummary scanSummary = scanRepositoryActivity.scanRepository(
        cloneResult.localPath());
    RepositoryCloneResponse response = RepositoryCloneResponse.temporal(cloneResult,
        Workflow.getInfo().getWorkflowId(), scanSummary);
    if (!generateDocumentation) {
      return response;
    }
    PromptExecutionResult executionResult = generateDocumentationActivity.generateDocumentation(
        buildDocumentationContext(request, cloneResult, scanSummary), templateId);
    return response.withPromptExecutionResult(executionResult);
  }

  private DocumentationGenerationContext buildDocumentationContext(RepositoryCloneRequest request,
      RepositoryCloneResult cloneResult, CodeScanSummary scanSummary) {
    return new DocumentationGenerationContext(scanSummary.scanId(), request.repositoryName(),
        cloneResult.localPath(), request.organization(), request.projectName(),
        request.pullRequestId(), scanSummary);
  }

  private static ActivityOptions cloneActivityOptions() {
    return ActivityOptions.newBuilder()
        .setStartToCloseTimeout(Duration.ofMinutes(10))
        .setRetryOptions(RetryOptions.newBuilder().setMaximumAttempts(3).build())
        .build();
  }

  private static ActivityOptions scanActivityOptions() {
    return ActivityOptions.newBuilder()
        .setStartToCloseTimeout(Duration.ofMinutes(5))
        .setRetryOptions(RetryOptions.newBuilder().setMaximumAttempts(2).build())
        .build();
  }

  private static ActivityOptions generateDocumentationActivityOptions() {
    // Multiple sequential Copilot CLI calls (specialists + consolidation + render);
    // a single attempt avoids duplicate, costly LLM executions on failure.
    return ActivityOptions.newBuilder()
        .setStartToCloseTimeout(Duration.ofMinutes(45))
        .setRetryOptions(RetryOptions.newBuilder().setMaximumAttempts(1).build())
        .build();
  }
}