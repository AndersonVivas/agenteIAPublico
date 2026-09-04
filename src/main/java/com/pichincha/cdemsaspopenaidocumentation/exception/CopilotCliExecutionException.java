package com.pichincha.cdemsaspopenaidocumentation.exception;

public class CopilotCliExecutionException extends RuntimeException {

  public CopilotCliExecutionException(String message) {
    super(message);
  }

  public CopilotCliExecutionException(String message, Throwable cause) {
    super(message, cause);
  }
}
