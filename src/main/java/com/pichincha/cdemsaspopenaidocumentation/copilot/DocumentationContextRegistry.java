package com.pichincha.cdemsaspopenaidocumentation.copilot;

import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentationGenerationContext;
import java.util.Optional;

public interface DocumentationContextRegistry {

  void register(DocumentationGenerationContext context);

  Optional<DocumentationGenerationContext> findByScanId(String scanId);
}