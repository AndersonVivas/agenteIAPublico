package com.pichincha.cdemsaspopenaidocumentation.prompt.domain;

import com.pichincha.cdemsaspopenaidocumentation.domain.CodeSignalEvidence;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public record PromptContext(
    RepositoryMetadata repositoryMetadata,
    List<CodeSignalEvidence> evidences,
    String ragContext,
    String currentDocumentation,
    List<String> historicalIncidents) {

  public PromptContext {
    repositoryMetadata = repositoryMetadata == null
        ? new RepositoryMetadata("", "", "", "", Map.of())
        : repositoryMetadata;
    evidences = evidences == null ? List.of() : List.copyOf(evidences);
    ragContext = normalize(ragContext);
    currentDocumentation = normalize(currentDocumentation);
    historicalIncidents = historicalIncidents == null ? List.of() : List.copyOf(historicalIncidents);
  }

  private static String normalize(String value) {
    return StringUtils.defaultString(value).trim();
  }
}