package com.pichincha.cdemsaspopenaidocumentation.copilot.strategy;

import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentTemplateType;
import org.springframework.stereotype.Component;

@Component
public class ApiDocumentationDocumentTypeStrategy implements DocumentTypeStrategy {

  @Override
  public DocumentTemplateType supportedType() {
    return DocumentTemplateType.API_DOCUMENTATION;
  }

  @Override
  public String title() {
    return "API Documentation";
  }
}
