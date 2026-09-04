package com.pichincha.cdemsaspopenaidocumentation.controller;

import com.pichincha.cdemsaspopenaidocumentation.domain.ApiErrorResponse;
import com.pichincha.cdemsaspopenaidocumentation.exception.CopilotCliExecutionException;
import com.pichincha.cdemsaspopenaidocumentation.exception.RepositoryCloneException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {

  @ExceptionHandler(RepositoryCloneException.class)
  public ResponseEntity<ApiErrorResponse> handleRepositoryCloneException(
      RepositoryCloneException exception) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ApiErrorResponse(exception.getMessage()));
  }

  @ExceptionHandler(CopilotCliExecutionException.class)
  public ResponseEntity<ApiErrorResponse> handleCopilotCliExecutionException(
      CopilotCliExecutionException exception) {
    return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
        .body(new ApiErrorResponse(exception.getMessage()));
  }

  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<ApiErrorResponse> handleIllegalStateException(
      IllegalStateException exception) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ApiErrorResponse(exception.getMessage()));
  }
}