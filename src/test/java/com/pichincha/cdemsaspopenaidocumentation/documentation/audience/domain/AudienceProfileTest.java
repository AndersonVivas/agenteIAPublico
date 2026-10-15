package com.pichincha.cdemsaspopenaidocumentation.documentation.audience.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;

class AudienceProfileTest {

  @Test
  void shouldExposeDefaultProfilesForAllAudienceTypes() {
    Map<AudienceType, AudienceProfile> defaults = AudienceProfile.defaults();

    assertEquals(6, defaults.size());
    assertTrue(defaults.containsKey(AudienceType.OPERATIONS));
    assertTrue(defaults.containsKey(AudienceType.SECURITY));
  }

  @Test
  void shouldNormalizeProfileValues() {
    AudienceProfile profile = new AudienceProfile(AudienceType.DEVELOPERS,
        " Developers ", " en ", " Impl ", " Fix ",
        java.util.List.of(" Code ", " "), java.util.List.of("  "),
        " explicit ");

    assertEquals("Developers", profile.displayName());
    assertEquals("en", profile.preferredLanguage());
    assertEquals(1, profile.primaryConcerns().size());
    assertTrue(profile.excludedConcerns().isEmpty());
  }
}
