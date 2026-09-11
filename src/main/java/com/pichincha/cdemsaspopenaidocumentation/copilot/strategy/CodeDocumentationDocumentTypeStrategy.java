package com.pichincha.cdemsaspopenaidocumentation.copilot.strategy;

import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentTemplateType;
import org.springframework.stereotype.Component;

@Component
public class CodeDocumentationDocumentTypeStrategy implements DocumentTypeStrategy {

  @Override
  public DocumentTemplateType supportedType() {
    return DocumentTemplateType.CODE_DOCUMENTATION;
  }

  @Override
  public String title() {
    return "Code Documentation";
  }
}
