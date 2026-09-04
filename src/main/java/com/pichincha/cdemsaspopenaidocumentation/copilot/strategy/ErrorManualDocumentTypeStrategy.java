package com.pichincha.cdemsaspopenaidocumentation.copilot.strategy;

import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentTemplateType;
import org.springframework.stereotype.Component;

@Component
public class ErrorManualDocumentTypeStrategy implements DocumentTypeStrategy {

  @Override
  public DocumentTemplateType supportedType() {
    return DocumentTemplateType.ERROR_MANUAL;
  }

  @Override
  public String title() {
    return "Error Manual";
  }
}
