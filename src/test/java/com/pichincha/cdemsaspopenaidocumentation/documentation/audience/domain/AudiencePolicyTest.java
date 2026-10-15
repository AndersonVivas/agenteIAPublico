package com.pichincha.cdemsaspopenaidocumentation.documentation.audience.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;
import org.junit.jupiter.api.Test;

class AudiencePolicyTest {

  @Test
  void shouldIncludeDefaultAudienceInSupportedSet() {
    AudiencePolicy policy = new AudiencePolicy(AudienceType.SECURITY,
        EnumSet.of(AudienceType.DEVELOPERS), true, null);

    assertEquals(AudienceType.SECURITY, policy.defaultAudience());
    assertTrue(policy.supportedAudiences().contains(AudienceType.SECURITY));
    assertEquals(AudienceType.SECURITY, policy.fallbackAudience());
  }

  @Test
  void shouldBuildDefaultPolicy() {
    AudiencePolicy policy = AudiencePolicy.defaultPolicy();

    assertEquals(AudienceType.OPERATIONS, policy.defaultAudience());
    assertTrue(policy.supports(AudienceType.FUNCTIONAL_USERS));
  }
}
