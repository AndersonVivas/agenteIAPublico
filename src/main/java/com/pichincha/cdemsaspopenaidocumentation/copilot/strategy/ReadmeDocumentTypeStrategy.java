package com.pichincha.cdemsaspopenaidocumentation.copilot.strategy;

import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentTemplateType;
import org.springframework.stereotype.Component;

@Component
public class ReadmeDocumentTypeStrategy implements DocumentTypeStrategy {

  @Override
  public DocumentTemplateType supportedType() {
    return DocumentTemplateType.README;
  }

  @Override
  public String title() {
    return "Repository README";
  }
}
