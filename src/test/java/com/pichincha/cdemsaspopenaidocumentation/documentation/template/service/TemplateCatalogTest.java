package com.pichincha.cdemsaspopenaidocumentation.documentation.template.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.DocumentTemplate;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateMetadata;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateVersion;
import com.pichincha.cdemsaspopenaidocumentation.domain.enums.TemplateStatusEnum;
import java.util.List;
import org.junit.jupiter.api.Test;

class TemplateCatalogTest {

  @Test
  void replaceAllShouldKeepHighestVersionPerTemplate() {
    TemplateCatalog catalog = new TemplateCatalog();

    catalog.replaceAll(List.of(template("error-manual", "1.0.0"),
        template("error-manual", "1.1.0"), template("pr-change-history", "1.0.0")));

    assertEquals(List.of("error-manual", "pr-change-history"), catalog.activeTemplateIds());
    assertEquals("1.1.0", catalog.resolveActive("error-manual").orElseThrow()
        .version().value());
  }

  @Test
  void replaceAllShouldForgetTemplatesNoLongerAvailable() {
    TemplateCatalog catalog = new TemplateCatalog();
    catalog.replaceAll(List.of(template("error-manual", "1.0.0")));

    catalog.replaceAll(List.of(template("pr-change-history", "1.0.0")));

    assertTrue(catalog.resolveActive("error-manual").isEmpty());
    assertEquals(List.of("pr-change-history"), catalog.activeTemplateIds());
  }

  @Test
  void replaceAllShouldExcludeTemplatesWhoseLatestVersionIsInactive() {
    TemplateCatalog catalog = new TemplateCatalog();

    catalog.replaceAll(List.of(template("error-manual", "1.0.0"),
        template("error-manual", "1.1.0", TemplateStatusEnum.INACTIVE),
        template("pr-change-history", "1.0.0")));

    assertTrue(catalog.resolveActive("error-manual").isEmpty());
    assertEquals(List.of("pr-change-history"), catalog.activeTemplateIds());
  }

  private DocumentTemplate template(String id, String version) {
    return template(id, version, TemplateStatusEnum.ACTIVE);
  }

  private DocumentTemplate template(String id, String version, TemplateStatusEnum status) {
    return new DocumentTemplate(id, "Error Manual", new TemplateVersion(version),
        List.of(), new TemplateMetadata("", status));
  }
}
