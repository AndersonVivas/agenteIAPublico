package com.pichincha.cdemsaspopenaidocumentation.config;

import com.pichincha.cdemsaspopenaidocumentation.activity.GenerateDocumentationActivity;
import com.pichincha.cdemsaspopenaidocumentation.activity.ScanRepositoryActivity;
import com.pichincha.cdemsaspopenaidocumentation.workflow.impl.RepositoryProcessingWorkflowImpl;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TemporalConfiguration {

  @Bean(destroyMethod = "shutdown")
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
      ScanRepositoryActivity scanRepositoryActivity,
      GenerateDocumentationActivity generateDocumentationActivity,
      TemporalProperties properties) {
    WorkerFactory workerFactory = WorkerFactory.newInstance(workflowClient);
    Worker worker = workerFactory.newWorker(properties.taskQueue());
    worker.registerWorkflowImplementationTypes(RepositoryProcessingWorkflowImpl.class);
    worker.registerActivitiesImplementations(scanRepositoryActivity,
        generateDocumentationActivity);
    workerFactory.start();
    return workerFactory;
  }
}
