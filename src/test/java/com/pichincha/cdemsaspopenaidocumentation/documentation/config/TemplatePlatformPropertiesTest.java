package com.pichincha.cdemsaspopenaidocumentation.documentation.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

class TemplatePlatformPropertiesTest {

  @Test
  void shouldDefaultToLegacyMode() {
    TemplatePlatformProperties properties = new TemplatePlatformProperties(null,
        false, false, false, false, "", null);

    assertEquals(DocumentationPlatformMode.LEGACY, properties.mode());
    assertFalse(properties.templateResolutionEnabled());
    assertFalse(properties.auditMetadataEnabled());
    assertFalse(properties.dualRegistrationEnabled());
    assertFalse(properties.localSourceEnabled());
    assertEquals(4, properties.localSourceExtensions().size());
  }
}
