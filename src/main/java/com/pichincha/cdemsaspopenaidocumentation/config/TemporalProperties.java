package com.pichincha.cdemsaspopenaidocumentation.config;

import java.time.Duration;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.temporal")
public record TemporalProperties(
    String namespace,
    String taskQueue,
    String serverTarget,
    Duration workflowTimeout,
    String workflowIdPrefix) {

  public TemporalProperties {
    namespace = StringUtils.defaultIfBlank(namespace, "default");
    taskQueue = StringUtils.defaultIfBlank(taskQueue, "repository-agent-task-queue");
    serverTarget = StringUtils.defaultIfBlank(serverTarget, "127.0.0.1:7233");
    workflowTimeout = workflowTimeout == null ? Duration.ofHours(1) : workflowTimeout;
    workflowIdPrefix = StringUtils.defaultIfBlank(workflowIdPrefix, "repository-agent");
  }
}
