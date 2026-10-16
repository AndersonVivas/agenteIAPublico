package com.pichincha.cdemsaspopenaidocumentation.documentation.template.exception;

public class TemplateNotFoundException extends RuntimeException {

  public TemplateNotFoundException(String templateId) {
    super("Template is not registered or is invalid: " + templateId);
  }
}
