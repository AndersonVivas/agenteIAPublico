package com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;

class TemplateMetadataTest {

  @Test
  void shouldApplyDefaultsWhenNullsAreProvided() {
    TemplateMetadata metadata = new TemplateMetadata(null, null, null, null, null);

    assertEquals("internal", metadata.source());
    assertEquals("", metadata.sourceReference());
    assertEquals(Instant.EPOCH, metadata.publishedAt());
    assertTrue(metadata.tags().isEmpty());
    assertTrue(metadata.attributes().isEmpty());
  }

  @Test
  void shouldCopyAttributesDefensively() {
    Map<String, String> attributes = Map.of("owner", "platform-team");
    TemplateMetadata metadata = new TemplateMetadata("git", "main", Instant.now(),
        null, attributes);

    assertEquals("platform-team", metadata.attributes().get("owner"));
  }
}
