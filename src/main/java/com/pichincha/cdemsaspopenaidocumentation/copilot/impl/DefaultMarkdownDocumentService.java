package com.pichincha.cdemsaspopenaidocumentation.copilot.impl;

import com.pichincha.cdemsaspopenaidocumentation.copilot.MarkdownDocumentService;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentTemplateType;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.PromptExecutionRequest;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.RepositoryDocumentSnapshot;
import com.pichincha.cdemsaspopenaidocumentation.copilot.strategy.DocumentTypeStrategy;
import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class DefaultMarkdownDocumentService implements MarkdownDocumentService {

  private static final String PROVIDER = "github-copilot-cli";
  private final Map<DocumentTemplateType, DocumentTypeStrategy> strategies;

  public DefaultMarkdownDocumentService(List<DocumentTypeStrategy> strategies) {
    this.strategies = new EnumMap<>(DocumentTemplateType.class);
    for (DocumentTypeStrategy strategy : strategies) {
      this.strategies.put(strategy.supportedType(), strategy);
    }
  }

  @Override
  public String buildDocumentName(String repositoryName) {
    String normalized = repositoryName.trim().toLowerCase();
    String cleaned = normalized.replaceAll("[^a-z0-9._-]", "-");
    return cleaned + ".md";
  }

  @Override
  public int resolveDocumentVersion(
      Optional<RepositoryDocumentSnapshot> previousDocument) {
    return previousDocument.map(snapshot -> snapshot.version())
        .map(version -> version + 1)
        .orElse(1);
  }

  @Override
  public String createMarkdown(PromptExecutionRequest request, String generatedContent,
      String commitHash, int version, Instant generatedAt) {
    String title = resolveTitle(request.templateType());
    String body = StringUtils.defaultString(generatedContent).trim();
    return String.join("\n", "---",
        "generated-at: " + generatedAt,
        "repository: " + request.repositoryName(),
        "commit-hash: " + commitHash,
        "document-type: " + request.templateType(),
        "document-version: " + version,
        "provider: " + PROVIDER,
        "---", "", "# " + title, "", body, "");
  }

  private String resolveTitle(DocumentTemplateType templateType) {
    DocumentTypeStrategy strategy = strategies.get(templateType);
    if (strategy == null) {
      throw new IllegalArgumentException("Unsupported template type: " + templateType);
    }
    return strategy.title();
  }
}
