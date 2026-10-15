package com.pichincha.cdemsaspopenaidocumentation.copilot.exception;

public class PromptNotFoundException extends RuntimeException {

  public PromptNotFoundException(String promptId, String path) {
    super("No external prompt was found for '" + promptId + "' at " + path);
  }
}
