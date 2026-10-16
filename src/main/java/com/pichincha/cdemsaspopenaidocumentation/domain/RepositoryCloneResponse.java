package com.pichincha.cdemsaspopenaidocumentation.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record RepositoryCloneResponse(
    String repositoryName,
    Long pullRequestId,
    String workflowId,
    ScanExecutionSummary scan,
    List<DocumentGenerationResult> documents) {

  public RepositoryCloneResponse {
    documents = documents == null ? List.of() : List.copyOf(documents);
  }

  public static RepositoryCloneResponse scanned(DocumentationRunRequest request,
      String workflowId, ScanExecutionSummary scan) {
    return new RepositoryCloneResponse(request.repository().repositoryName(),
        request.pullRequestId(), workflowId, scan, List.of());
  }

  public RepositoryCloneResponse withDocuments(List<DocumentGenerationResult> generated) {
    return new RepositoryCloneResponse(repositoryName, pullRequestId, workflowId, scan,
        generated);
  }
}
