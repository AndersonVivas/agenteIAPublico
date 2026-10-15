package com.pichincha.cdemsaspopenaidocumentation.documentation.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class DocumentationTemplatesPropertiesTest {

  @Test
  void shouldUseDefaultExtensions() {
    DocumentationTemplatesProperties properties =
        new DocumentationTemplatesProperties("", null);

    assertFalse(properties.enabled());
    assertEquals(4, properties.extensions().size());
  }

  @Test
  void shouldEnableWhenPathIsConfigured() {
    DocumentationTemplatesProperties properties =
        new DocumentationTemplatesProperties(" /tmp/templates ",
            java.util.List.of(".yaml"));

    assertTrue(properties.enabled());
    assertEquals("/tmp/templates", properties.path());
  }
}