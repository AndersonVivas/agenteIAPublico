package com.pichincha.cdemsaspopenaidocumentation.prompt;

import com.pichincha.cdemsaspopenaidocumentation.domain.CodeScanSummary;
import com.pichincha.cdemsaspopenaidocumentation.domain.RepositoryCloneRequest;
import com.pichincha.cdemsaspopenaidocumentation.domain.RepositoryCloneResponse;
import com.pichincha.cdemsaspopenaidocumentation.prompt.domain.PromptType;

public interface PromptPreviewService {

  String buildPreview(RepositoryCloneRequest request, RepositoryCloneResponse response,
      PromptType promptType);

  String buildPreview(RepositoryCloneRequest request, CodeScanSummary codeScanSummary,
      String localPath, PromptType promptType);
}