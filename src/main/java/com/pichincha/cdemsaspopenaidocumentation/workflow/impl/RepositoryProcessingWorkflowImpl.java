package com.pichincha.cdemsaspopenaidocumentation.workflow.impl;

import com.pichincha.cdemsaspopenaidocumentation.activity.CloneRepositoryActivity;
import com.pichincha.cdemsaspopenaidocumentation.domain.RepositoryCloneRequest;
import com.pichincha.cdemsaspopenaidocumentation.domain.RepositoryCloneResponse;
import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Workflow;
import java.time.Duration;

public class RepositoryProcessingWorkflowImpl implements
    com.pichincha.cdemsaspopenaidocumentation.workflow.RepositoryProcessingWorkflow {

  private final CloneRepositoryActivity cloneRepositoryActivity = Workflow.newActivityStub(
      CloneRepositoryActivity.class,
      ActivityOptions.newBuilder()
          .setStartToCloseTimeout(Duration.ofMinutes(10))
          .build());

  @Override
  public RepositoryCloneResponse process(RepositoryCloneRequest request) {
    return RepositoryCloneResponse.temporal(cloneRepositoryActivity.cloneRepository(request),
        Workflow.getInfo().getWorkflowId());
  }
}