package com.pichincha.cdemsaspopenaidocumentation.activity.impl;

import com.pichincha.cdemsaspopenaidocumentation.activity.GenerateDocumentationActivity;
import com.pichincha.cdemsaspopenaidocumentation.copilot.DocumentationContextRegistry;
import com.pichincha.cdemsaspopenaidocumentation.copilot.DocumentationGenerationService;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentationGenerationContext;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.PromptExecutionResult;
import com.pichincha.cdemsaspopenaidocumentation.domain.DocumentationGenerationRequest;
import org.springframework.stereotype.Component;

@Component
public class GenerateDocumentationActivityImpl implements GenerateDocumentationActivity {

  private final DocumentationContextRegistry documentationContextRegistry;
  private final DocumentationGenerationService documentationGenerationService;

  public GenerateDocumentationActivityImpl(
      DocumentationContextRegistry documentationContextRegistry,
      DocumentationGenerationService documentationGenerationService) {
    this.documentationContextRegistry = documentationContextRegistry;
    this.documentationGenerationService = documentationGenerationService;
  }

  @Override
  public PromptExecutionResult generateDocumentation(DocumentationGenerationContext context,
      String templateId) {
    documentationContextRegistry.register(context);
    return documentationGenerationService.generate(
        new DocumentationGenerationRequest(context.scanId(), templateId));
  }
}
