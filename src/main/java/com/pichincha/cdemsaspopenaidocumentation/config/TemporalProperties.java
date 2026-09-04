package com.pichincha.cdemsaspopenaidocumentation.config;

import java.time.Duration;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.temporal")
public record TemporalProperties(
    boolean enabled,
    String namespace,
    String taskQueue,
    String serverTarget,
    Duration workflowTimeout,
    String workflowIdPrefix) {

  public TemporalProperties {
    namespace = defaultIfBlank(namespace, "default");
    taskQueue = defaultIfBlank(taskQueue, "repository-agent-task-queue");
    serverTarget = defaultIfBlank(serverTarget, "127.0.0.1:7233");
    workflowTimeout = workflowTimeout == null ? Duration.ofMinutes(30) : workflowTimeout;
    workflowIdPrefix = defaultIfBlank(workflowIdPrefix, "repository-agent");
  }

  private static String defaultIfBlank(String value, String defaultValue) {
    return StringUtils.defaultIfBlank(value, defaultValue);
  }
}