package com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class TemplateResolutionResultTest {

  @Test
  void shouldRejectNullTemplate() {
    IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> new TemplateResolutionResult(null, null, "", false));

    assertTrue(exception.getMessage().contains("Template is required"));
  }

  @Test
  void shouldCreateFallbackResult() {
    DocumentTemplate template = new DocumentTemplate("error-manual", "Error Manual",
        new TemplateVersion("1.0.0"), List.of(), TemplateMetadata.internal());

    TemplateResolutionResult result = TemplateResolutionResult.fallback(template,
        "local-cache");

    assertTrue(result.fallbackApplied());
    assertEquals("1.0.0", result.resolvedVersion().value());
    assertEquals("local-cache", result.resolvedFrom());
  }
}
