package com.pichincha.cdemsaspopenaidocumentation.activity.impl;

import com.pichincha.cdemsaspopenaidocumentation.activity.GenerateDocumentationActivity;
import com.pichincha.cdemsaspopenaidocumentation.copilot.DocumentationGenerationService;
import com.pichincha.cdemsaspopenaidocumentation.domain.DocumentGenerationResult;
import com.pichincha.cdemsaspopenaidocumentation.domain.DocumentationGenerationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GenerateDocumentationActivityImpl implements GenerateDocumentationActivity {

  private final DocumentationGenerationService documentationGenerationService;

  @Override
  public DocumentGenerationResult generateDocumentation(DocumentationGenerationRequest request) {
    return documentationGenerationService.generate(request);
  }
}
