package com.pichincha.cdemsaspopenaidocumentation.documentation.template.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.RawTemplateDescriptor;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateFormat;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateSourceType;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateValidationResult;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateVersion;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class TemplateValidatorTest {

  @Test
  void shouldValidateDescriptorAndCreateTemplate() {
    TemplateValidator validator = new TemplateValidator();
    RawTemplateDescriptor descriptor = new RawTemplateDescriptor("error.yaml",
        "error-manual", "Error Manual", new TemplateVersion("1.0.0"),
        """
            name: Error Manual
            tags:
              - operations
            sections:
              - id: symptoms
                title: Symptoms
                objective: Describe the failure
                instructions:
                  - Include exact symptom
            """, TemplateFormat.YAML, TemplateSourceType.LOCAL,
        "/tmp/error.yaml", "", Instant.now());

    TemplateValidationResult result = validator.validate(descriptor);

    assertTrue(result.valid());
    assertEquals("error-manual", result.normalizedTemplate().id());
    assertEquals("1.0.0", result.normalizedTemplate().version().value());
    assertEquals(1, result.normalizedTemplate().sections().size());
    assertEquals("operations", result.normalizedTemplate().metadata().tags().getFirst());
    assertTrue(result.errors().isEmpty());
  }

  @Test
  void shouldDeriveMarkdownTemplateSectionsFromHeadings() {
    TemplateValidator validator = new TemplateValidator();
    RawTemplateDescriptor descriptor = new RawTemplateDescriptor("api.md",
        "", "", new TemplateVersion("2.1.0"),
        """
            ---
            templateId: api-template
            name: API Template
            ---

            # API Template

            ## Overview
            Some context.

            ## Endpoints
            More details.
            """, TemplateFormat.MARKDOWN, TemplateSourceType.FILE_SYSTEM,
        "/tmp/api.md", "", Instant.now());

    TemplateValidationResult result = validator.validate(descriptor);

    assertTrue(result.valid());
    assertEquals("api-template", result.normalizedTemplate().id());
    assertEquals(2, result.normalizedTemplate().sections().size());
    assertEquals("Overview", result.normalizedTemplate().sections().getFirst().title());
  }

  @Test
  void shouldRejectUnsupportedFormat() {
    TemplateValidator validator = new TemplateValidator();
    RawTemplateDescriptor descriptor = new RawTemplateDescriptor("error.txt",
        "error-manual", "Error Manual", new TemplateVersion("1.0.0"), "",
        TemplateFormat.UNKNOWN, TemplateSourceType.LOCAL, "/tmp/error.txt", "",
        Instant.now());

    TemplateValidationResult result = validator.validate(descriptor);

    assertFalse(result.valid());
    assertTrue(result.errors().stream().anyMatch(error ->
        error.contains("unsupported")));
  }
}
