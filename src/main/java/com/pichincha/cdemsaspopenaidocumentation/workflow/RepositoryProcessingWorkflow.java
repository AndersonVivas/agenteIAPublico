package com.pichincha.cdemsaspopenaidocumentation.workflow;

import com.pichincha.cdemsaspopenaidocumentation.domain.DocumentationRunRequest;
import com.pichincha.cdemsaspopenaidocumentation.domain.RepositoryCloneResponse;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface RepositoryProcessingWorkflow {

  @WorkflowMethod
  RepositoryCloneResponse process(DocumentationRunRequest request);
}
