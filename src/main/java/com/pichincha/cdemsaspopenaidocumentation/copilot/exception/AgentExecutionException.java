package com.pichincha.cdemsaspopenaidocumentation.copilot.exception;

import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentationAgentType;

public class AgentExecutionException extends RuntimeException {

  public AgentExecutionException(DocumentationAgentType agentType, String reason) {
    super("Agent " + agentType + " failed: " + reason);
  }

  public AgentExecutionException(DocumentationAgentType agentType, String reason,
      Throwable cause) {
    super("Agent " + agentType + " failed: " + reason, cause);
  }

  public AgentExecutionException(String agentLabel, String reason) {
    super("Agent " + agentLabel + " failed: " + reason);
  }

  public AgentExecutionException(String agentLabel, String reason, Throwable cause) {
    super("Agent " + agentLabel + " failed: " + reason, cause);
  }
}
