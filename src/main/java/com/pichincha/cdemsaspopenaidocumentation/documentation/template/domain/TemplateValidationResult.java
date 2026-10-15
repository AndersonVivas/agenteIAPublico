package com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain;

import java.util.List;

public record TemplateValidationResult(
    boolean valid,
    DocumentTemplate normalizedTemplate,
    List<String> errors,
    List<String> warnings) {

  public TemplateValidationResult {
    errors = errors == null ? List.of() : List.copyOf(errors);
    warnings = warnings == null ? List.of() : List.copyOf(warnings);
  }

  public static TemplateValidationResult invalid(List<String> errors,
      List<String> warnings) {
    return new TemplateValidationResult(false, null, errors, warnings);
  }

  public static TemplateValidationResult valid(DocumentTemplate template,
      List<String> warnings) {
    return new TemplateValidationResult(true, template, List.of(), warnings);
  }
}
