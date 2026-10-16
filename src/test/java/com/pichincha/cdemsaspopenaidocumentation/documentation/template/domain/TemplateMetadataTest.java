package com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pichincha.cdemsaspopenaidocumentation.domain.enums.TemplateStatusEnum;
import org.junit.jupiter.api.Test;

class TemplateMetadataTest {

  @Test
  void shouldNormalizeMissingChecksum() {
    assertEquals("", new TemplateMetadata(null, null).checksum());
    assertEquals("", TemplateMetadata.internal().checksum());
  }

  @Test
  void shouldTrimChecksum() {
    assertEquals("abc", new TemplateMetadata(" abc ", null).checksum());
  }

  @Test
  void shouldBeActiveUnlessDeclaredInactive() {
    assertTrue(new TemplateMetadata("abc", null).active());
    assertFalse(new TemplateMetadata("abc", TemplateStatusEnum.INACTIVE).active());
  }
}
