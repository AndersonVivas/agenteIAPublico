package com.pichincha.cdemsaspopenaidocumentation.copilot;

import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.PromptExecutionRequest;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.RepositoryDocumentSnapshot;
import java.time.Instant;
import java.util.Optional;

public interface MarkdownDocumentService {

  String buildDocumentName(String repositoryName);

  int resolveDocumentVersion(Optional<RepositoryDocumentSnapshot> previousDocument);

  String createMarkdown(PromptExecutionRequest request, String generatedContent,
      String commitHash, int version, Instant generatedAt);
}
