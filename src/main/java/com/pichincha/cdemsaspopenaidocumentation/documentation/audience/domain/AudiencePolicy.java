package com.pichincha.cdemsaspopenaidocumentation.documentation.audience.domain;

import java.util.EnumSet;
import java.util.Set;

public record AudiencePolicy(
    AudienceType defaultAudience,
    Set<AudienceType> supportedAudiences,
    boolean overrideAllowed,
    AudienceType fallbackAudience) {

  public AudiencePolicy {
    defaultAudience = defaultAudience == null ? AudienceType.OPERATIONS
        : defaultAudience;
    supportedAudiences = normalizeSupported(supportedAudiences, defaultAudience);
    fallbackAudience = fallbackAudience == null ? defaultAudience : fallbackAudience;
  }

  public boolean supports(AudienceType audienceType) {
    return supportedAudiences.contains(audienceType);
  }

  public static AudiencePolicy defaultPolicy() {
    return new AudiencePolicy(AudienceType.OPERATIONS,
        EnumSet.allOf(AudienceType.class), true, AudienceType.OPERATIONS);
  }

  private static Set<AudienceType> normalizeSupported(Set<AudienceType> supportedAudiences,
      AudienceType defaultAudience) {
    if (supportedAudiences == null || supportedAudiences.isEmpty()) {
      return EnumSet.of(defaultAudience);
    }
    Set<AudienceType> copy = EnumSet.copyOf(supportedAudiences);
    copy.add(defaultAudience);
    return copy;
  }
}
