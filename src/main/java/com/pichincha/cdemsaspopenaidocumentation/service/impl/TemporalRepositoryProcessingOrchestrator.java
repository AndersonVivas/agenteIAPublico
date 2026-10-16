package com.pichincha.cdemsaspopenaidocumentation.service.impl;

import com.pichincha.cdemsaspopenaidocumentation.config.TemporalProperties;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.service.DocumentTemplateService;
import com.pichincha.cdemsaspopenaidocumentation.domain.DocumentationRunRequest;
import com.pichincha.cdemsaspopenaidocumentation.domain.RepositoryCloneRequest;
import com.pichincha.cdemsaspopenaidocumentation.domain.RepositoryCloneResponse;
import com.pichincha.cdemsaspopenaidocumentation.domain.RepositoryCoordinates;
import com.pichincha.cdemsaspopenaidocumentation.exception.ExecutionAlreadyRunningException;
import com.pichincha.cdemsaspopenaidocumentation.service.RepositoryProcessingOrchestrator;
import com.pichincha.cdemsaspopenaidocumentation.workflow.RepositoryProcessingWorkflow;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowExecutionAlreadyStarted;
import io.temporal.client.WorkflowOptions;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TemporalRepositoryProcessingOrchestrator implements RepositoryProcessingOrchestrator {

  private final WorkflowClient workflowClient;
  private final TemporalProperties properties;
  private final DocumentTemplateService documentTemplateService;

  @Override
  public RepositoryCloneResponse process(RepositoryCloneRequest request,
      boolean generateDocumentation) {
    List<String> templateIds = generateDocumentation
        ? documentTemplateService.resolveTemplateIds(request.templateIds()) : List.of();
    String workflowId = workflowId(request.coordinates());
    RepositoryProcessingWorkflow workflow = workflowClient.newWorkflowStub(
        RepositoryProcessingWorkflow.class, workflowOptions(workflowId));
    try {
      return workflow.process(new DocumentationRunRequest(request.coordinates(),
          request.pullRequestId(), templateIds, request.forceFullScan(),
          generateDocumentation));
    } catch (WorkflowExecutionAlreadyStarted exception) {
      throw new ExecutionAlreadyRunningException(workflowId, exception);
    }
  }

  private WorkflowOptions workflowOptions(String workflowId) {
    return WorkflowOptions.newBuilder()
        .setTaskQueue(properties.taskQueue())
        .setWorkflowId(workflowId)
        .setWorkflowExecutionTimeout(properties.workflowTimeout())
        .build();
  }

  private String workflowId(RepositoryCoordinates repository) {
    return properties.workflowIdPrefix() + "-" + repository.key().replace('/', '-');
  }
}
