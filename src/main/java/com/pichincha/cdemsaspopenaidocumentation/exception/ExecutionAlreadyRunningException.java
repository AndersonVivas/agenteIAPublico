package com.pichincha.cdemsaspopenaidocumentation.exception;

public class ExecutionAlreadyRunningException extends RuntimeException {

  public ExecutionAlreadyRunningException(String workflowId, Throwable cause) {
    super("A documentation execution is already running for workflow " + workflowId
        + ". Retry when it finishes.", cause);
  }
}
