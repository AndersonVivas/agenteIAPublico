package com.pichincha.cdemsaspopenaidocumentation.controller;

import com.pichincha.cdemsaspopenaidocumentation.documentation.template.exception.TemplateNotFoundException;
import com.pichincha.cdemsaspopenaidocumentation.domain.ApiErrorResponse;
import com.pichincha.cdemsaspopenaidocumentation.exception.ExecutionAlreadyRunningException;
import com.pichincha.cdemsaspopenaidocumentation.exception.RepositoryCloneException;
import io.temporal.client.WorkflowFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {

  @ExceptionHandler(TemplateNotFoundException.class)
  public ResponseEntity<ApiErrorResponse> handleTemplateNotFound(
      TemplateNotFoundException exception) {
    return error(HttpStatus.BAD_REQUEST, exception.getMessage());
  }

  @ExceptionHandler(ExecutionAlreadyRunningException.class)
  public ResponseEntity<ApiErrorResponse> handleExecutionAlreadyRunning(
      ExecutionAlreadyRunningException exception) {
    return error(HttpStatus.CONFLICT, exception.getMessage());
  }

  @ExceptionHandler(WorkflowFailedException.class)
  public ResponseEntity<ApiErrorResponse> handleWorkflowFailed(
      WorkflowFailedException exception) {
    return error(HttpStatus.BAD_GATEWAY, rootMessage(exception));
  }

  @ExceptionHandler(RepositoryCloneException.class)
  public ResponseEntity<ApiErrorResponse> handleRepositoryClone(
      RepositoryCloneException exception) {
    return error(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage());
  }

  private ResponseEntity<ApiErrorResponse> error(HttpStatus status, String message) {
    return ResponseEntity.status(status).body(new ApiErrorResponse(message));
  }

  private String rootMessage(Throwable exception) {
    Throwable cause = exception;
    while (cause.getCause() != null) {
      cause = cause.getCause();
    }
    return cause.getMessage();
  }
}
