package com.pichincha.cdemsaspopenaidocumentation.copilot.strategy;

import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentTemplateType;
import org.springframework.stereotype.Component;

@Component
public class TechnicalDocumentationDocumentTypeStrategy implements DocumentTypeStrategy {

  @Override
  public DocumentTemplateType supportedType() {
    return DocumentTemplateType.TECHNICAL_DOCUMENTATION;
  }

  @Override
  public String title() {
    return "Technical Documentation";
  }
}
