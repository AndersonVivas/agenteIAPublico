package com.pichincha.cdemsaspopenaidocumentation.copilot.strategy;

import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentTemplateType;

public interface DocumentTypeStrategy {

  DocumentTemplateType supportedType();

  String title();
}
