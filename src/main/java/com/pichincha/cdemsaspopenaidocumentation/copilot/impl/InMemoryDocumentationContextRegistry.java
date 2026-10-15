package com.pichincha.cdemsaspopenaidocumentation.copilot.impl;

import com.pichincha.cdemsaspopenaidocumentation.copilot.DocumentationContextRegistry;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentationGenerationContext;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class InMemoryDocumentationContextRegistry implements DocumentationContextRegistry {

  private final Map<String, DocumentationGenerationContext> contextsByScanId;

  public InMemoryDocumentationContextRegistry() {
    this.contextsByScanId = new ConcurrentHashMap<>();
  }

  @Override
  public void register(DocumentationGenerationContext context) {
    if (context == null || StringUtils.isBlank(context.scanId())) {
      return;
    }
    contextsByScanId.put(context.scanId(), context);
  }

  @Override
  public Optional<DocumentationGenerationContext> findByScanId(String scanId) {
    String normalizedScanId = StringUtils.defaultString(scanId).trim();
    return Optional.ofNullable(contextsByScanId.get(normalizedScanId));
  }
}