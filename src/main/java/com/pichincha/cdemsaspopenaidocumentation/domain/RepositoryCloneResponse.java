package com.pichincha.cdemsaspopenaidocumentation.domain;

import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.PromptExecutionResult;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record RepositoryCloneResponse(
    String repositoryName,
    Long pullRequestId,
    String localPath,
    String status,
    String orchestrationMode,
    String workflowId,
    List<String> stages,
    CodeScanSummary codeScanSummary,
    String promptPreview,
    PromptExecutionResult promptExecutionResult) {

  public static RepositoryCloneResponse direct(RepositoryCloneResult result,
      CodeScanSummary codeScanSummary) {
    return new RepositoryCloneResponse(result.repositoryName(), result.pullRequestId(),
        result.localPath(), result.status(), "DIRECT", null,
      List.of("CLONE", "SCAN_CODE"), codeScanSummary, null, null);
  }

  public static RepositoryCloneResponse temporal(RepositoryCloneResult result,
      String workflowId) {
    return new RepositoryCloneResponse(result.repositoryName(), result.pullRequestId(),
        result.localPath(), result.status(), "TEMPORAL", workflowId,
        List.of("CLONE"), null, null, null);
  }

  public RepositoryCloneResponse withPromptPreview(String promptPreview) {
    return new RepositoryCloneResponse(repositoryName, pullRequestId, localPath, status,
        orchestrationMode, workflowId, stages, codeScanSummary, promptPreview,
        promptExecutionResult);
  }

  public RepositoryCloneResponse withPromptExecutionResult(
      PromptExecutionResult promptExecutionResult) {
    return new RepositoryCloneResponse(repositoryName, pullRequestId, localPath, status,
        orchestrationMode, workflowId, stages, codeScanSummary, promptPreview,
        promptExecutionResult);
  }
}