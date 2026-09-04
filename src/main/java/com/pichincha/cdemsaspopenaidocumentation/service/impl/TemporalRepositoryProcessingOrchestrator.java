package com.pichincha.cdemsaspopenaidocumentation.service.impl;

import com.pichincha.cdemsaspopenaidocumentation.config.TemporalProperties;
import com.pichincha.cdemsaspopenaidocumentation.domain.RepositoryCloneRequest;
import com.pichincha.cdemsaspopenaidocumentation.domain.RepositoryCloneResponse;
import com.pichincha.cdemsaspopenaidocumentation.service.RepositoryProcessingOrchestrator;
import com.pichincha.cdemsaspopenaidocumentation.workflow.RepositoryProcessingWorkflow;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.temporal", name = "enabled", havingValue = "true")
public class TemporalRepositoryProcessingOrchestrator implements RepositoryProcessingOrchestrator {

  private final WorkflowClient workflowClient;
  private final TemporalProperties properties;

  public TemporalRepositoryProcessingOrchestrator(WorkflowClient workflowClient,
      TemporalProperties properties) {
    this.workflowClient = workflowClient;
    this.properties = properties;
  }

  @Override
  public RepositoryCloneResponse process(RepositoryCloneRequest request) {
    RepositoryProcessingWorkflow workflow = workflowClient.newWorkflowStub(
        RepositoryProcessingWorkflow.class, workflowOptions(request));
    return workflow.process(request);
  }

  private WorkflowOptions workflowOptions(RepositoryCloneRequest request) {
    return WorkflowOptions.newBuilder()
        .setTaskQueue(properties.taskQueue())
        .setWorkflowId(buildWorkflowId(request))
        .setWorkflowRunTimeout(properties.workflowTimeout())
        .build();
  }

  private String buildWorkflowId(RepositoryCloneRequest request) {
    return properties.workflowIdPrefix() + "-" + request.pullRequestId() + "-"
        + request.repositoryName().replaceAll("[^a-zA-Z0-9._-]", "-");
  }
}