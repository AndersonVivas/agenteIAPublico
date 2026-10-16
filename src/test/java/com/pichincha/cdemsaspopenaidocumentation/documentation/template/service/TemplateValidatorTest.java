package com.pichincha.cdemsaspopenaidocumentation.documentation.template.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pichincha.cdemsaspopenaidocumentation.documentation.audience.domain.AudienceType;
import com.pichincha.cdemsaspopenaidocumentation.documentation.communication.domain.PresentationMode;
import com.pichincha.cdemsaspopenaidocumentation.documentation.communication.domain.ReadingLevel;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.DocumentTemplate;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.RawTemplateDescriptor;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.ReaderProfile;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateFormat;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateValidationResult;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateVersion;
import com.pichincha.cdemsaspopenaidocumentation.domain.enums.DocumentLifecycleEnum;
import java.util.List;
import org.junit.jupiter.api.Test;

class TemplateValidatorTest {

  private final TemplateValidator validator = new TemplateValidator(
      new TemplateGuidanceParser(),
      new TemplateReaderParser(new TemplateCommunicationParser()));

  @Test
  void shouldValidateDescriptorAndCreateTemplate() {
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
            """, TemplateFormat.YAML, "");

    TemplateValidationResult result = validator.validate(descriptor);

    assertTrue(result.valid());
    assertEquals("error-manual", result.normalizedTemplate().id());
    assertEquals("1.0.0", result.normalizedTemplate().version().value());
    assertEquals(1, result.normalizedTemplate().sections().size());
    assertEquals(List.of("Include exact symptom"),
        result.normalizedTemplate().sections().getFirst().instructions());
    assertEquals(DocumentLifecycleEnum.EVOLUTIONARY,
        result.normalizedTemplate().guidance().lifecycle());
    assertTrue(result.errors().isEmpty());
  }

  @Test
  void shouldParseHistoricalLifecycleRulesAndMissingDataMessage() {
    TemplateValidationResult result = validator.validate(yamlDescriptor("""
        lifecycle: historical
        globalRules:
          - Describe only the change.
        outputContract:
          missingInformationText: Sin cambios.
        sections:
          - id: summary
            title: Summary
        """));

    assertTrue(result.valid());
    assertEquals(DocumentLifecycleEnum.HISTORICAL,
        result.normalizedTemplate().guidance().lifecycle());
    assertEquals(List.of("Describe only the change."),
        result.normalizedTemplate().guidance().globalRules());
    assertEquals("Sin cambios.", result.normalizedTemplate().guidance().missingDataMessage());
  }

  @Test
  void shouldRejectUnknownLifecycle() {
    TemplateValidationResult result = validator.validate(yamlDescriptor("lifecycle: weekly"));

    assertFalse(result.valid());
    assertTrue(result.errors().stream().anyMatch(error -> error.contains("lifecycle")));
  }

  @Test
  void shouldDeriveMarkdownTemplateSectionsFromHeadings() {
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
            """, TemplateFormat.MARKDOWN, "");

    TemplateValidationResult result = validator.validate(descriptor);

    assertTrue(result.valid());
    assertEquals("api-template", result.normalizedTemplate().id());
    assertEquals(2, result.normalizedTemplate().sections().size());
    assertEquals("Overview", result.normalizedTemplate().sections().getFirst().title());
  }

  @Test
  void shouldRejectUnsupportedFormat() {
    RawTemplateDescriptor descriptor = new RawTemplateDescriptor("error.txt",
        "error-manual", "Error Manual", new TemplateVersion("1.0.0"), "",
        TemplateFormat.UNKNOWN, "");

    TemplateValidationResult result = validator.validate(descriptor);

    assertFalse(result.valid());
    assertTrue(result.errors().stream().anyMatch(error ->
        error.contains("unsupported")));
  }

  @Test
  void shouldParseReaderSkillsAndEditorialReviewFromYaml() {
    TemplateValidationResult result = validator.validate(yamlDescriptor("""
        editorialReview: true
        skills:
          - lenguaje-claro
        audience: functional_users
        audienceProfile:
          displayName: Business users
        communicationProfile:
          presentationMode: CATALOG
        sections:
          - id: summary
            title: Summary
        """));

    DocumentTemplate template = result.normalizedTemplate();
    assertTrue(result.valid());
    assertTrue(template.guidance().editorialReview());
    assertEquals(List.of("lenguaje-claro"), template.guidance().skills());
    assertEquals(AudienceType.FUNCTIONAL_USERS, template.reader().audience().audienceType());
    assertEquals("Business users", template.reader().audience().displayName());
    assertEquals(PresentationMode.CATALOG, template.reader().communication().presentationMode());
  }

  @Test
  void shouldKeepTheBuiltInProfileOfTheAudienceForUndeclaredFields() {
    TemplateValidationResult result = validator.validate(yamlDescriptor("""
        audience: FUNCTIONAL_USERS
        communicationProfile:
          presentationMode: CATALOG
        sections:
          - id: summary
            title: Summary
        """));

    ReaderProfile reader = result.normalizedTemplate().reader();
    ReaderProfile builtIn = ReaderProfile.defaultFor(AudienceType.FUNCTIONAL_USERS);
    assertEquals(builtIn.audience(), reader.audience());
    assertEquals(ReadingLevel.FUNCTIONAL, reader.communication().readingLevel());
    assertEquals(builtIn.communication().forbiddenTerms(),
        reader.communication().forbiddenTerms());
    assertFalse(result.normalizedTemplate().guidance().editorialReview());
  }

  @Test
  void shouldReadTemplateStatusAndWarnAboutUnsupportedKeys() {
    TemplateValidationResult result = validator.validate(yamlDescriptor("""
        status: inactive
        writingInstructions:
          tone: neutral
        outputContract:
          missingInformationText: Sin datos.
          format: markdown
        sections:
          - id: summary
            title: Summary
        """));

    assertTrue(result.valid());
    assertFalse(result.normalizedTemplate().metadata().active());
    assertEquals(List.of("Unsupported template key 'writingInstructions' is ignored.",
        "Unsupported template key 'outputContract.format' is ignored."), result.warnings());
  }

  @Test
  void shouldRejectUnknownAudienceCommunicationAndStatusValues() {
    TemplateValidationResult result = validator.validate(yamlDescriptor("""
        audience: MARKETING
        status: archived
        communicationProfile:
          readingLevel: CHILDREN
        sections:
          - id: summary
            title: Summary
        """));

    assertFalse(result.valid());
    assertTrue(result.errors().stream().anyMatch(error -> error.contains("MARKETING")));
    assertTrue(result.errors().stream().anyMatch(error -> error.contains("CHILDREN")));
    assertTrue(result.errors().stream().anyMatch(error -> error.contains("archived")));
  }

  private RawTemplateDescriptor yamlDescriptor(String content) {
    return new RawTemplateDescriptor("history.yaml", "history", "History",
        new TemplateVersion("1.0.0"), content, TemplateFormat.YAML, "");
  }
}
