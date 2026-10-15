package com.pichincha.cdemsaspopenaidocumentation.documentation.template.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.DocumentTemplate;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateMetadata;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateSourceType;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateVersion;
import java.util.List;
import org.junit.jupiter.api.Test;

class TemplateCatalogTest {

  @Test
  void shouldPublishNewVersionAsActive() {
    TemplateCatalog catalog = new TemplateCatalog();

    boolean insertedV1 = catalog.publish(template("error-manual", "1.0.0"),
        "local", TemplateSourceType.LOCAL, true);
    boolean insertedV2 = catalog.publish(template("error-manual", "1.1.0"),
        "local", TemplateSourceType.LOCAL, true);

    assertTrue(insertedV1);
    assertTrue(insertedV2);
    assertEquals("1.1.0", catalog.resolveActive("error-manual").orElseThrow()
        .resolvedVersion().value());
  }

  @Test
  void shouldSkipDuplicateVersion() {
    TemplateCatalog catalog = new TemplateCatalog();

    boolean first = catalog.publish(template("error-manual", "1.0.0"),
        "local", TemplateSourceType.LOCAL, true);
    boolean second = catalog.publish(template("error-manual", "1.0.0"),
        "local", TemplateSourceType.LOCAL, true);

    assertTrue(first);
    assertFalse(second);
    assertEquals(1, catalog.entries().size());
  }

  private DocumentTemplate template(String id, String version) {
    return new DocumentTemplate(id, "Error Manual", new TemplateVersion(version),
        List.of(), TemplateMetadata.internal());
  }
}
