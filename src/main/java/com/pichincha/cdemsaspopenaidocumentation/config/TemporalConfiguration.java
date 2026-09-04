package com.pichincha.cdemsaspopenaidocumentation.config;

import com.pichincha.cdemsaspopenaidocumentation.activity.CloneRepositoryActivity;
import com.pichincha.cdemsaspopenaidocumentation.workflow.impl.RepositoryProcessingWorkflowImpl;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "app.temporal", name = "enabled", havingValue = "true")
public class TemporalConfiguration {

  @Bean
  WorkflowServiceStubs workflowServiceStubs(TemporalProperties properties) {
    WorkflowServiceStubsOptions options = WorkflowServiceStubsOptions.newBuilder()
        .setTarget(properties.serverTarget())
        .build();
    return WorkflowServiceStubs.newServiceStubs(options);
  }

  @Bean
  WorkflowClient workflowClient(WorkflowServiceStubs workflowServiceStubs,
      TemporalProperties properties) {
    WorkflowClientOptions options = WorkflowClientOptions.newBuilder()
        .setNamespace(properties.namespace())
        .build();
    return WorkflowClient.newInstance(workflowServiceStubs, options);
  }

  @Bean(destroyMethod = "shutdown")
  WorkerFactory workerFactory(WorkflowClient workflowClient,
      CloneRepositoryActivity cloneRepositoryActivity,
      TemporalProperties properties) {
    WorkerFactory workerFactory = WorkerFactory.newInstance(workflowClient);
    Worker worker = workerFactory.newWorker(properties.taskQueue());
    worker.registerActivitiesImplementations(cloneRepositoryActivity);
    worker.registerWorkflowImplementationTypes(RepositoryProcessingWorkflowImpl.class);
    workerFactory.start();
    return workerFactory;
  }
}