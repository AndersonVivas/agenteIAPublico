package com.pichincha.cdemsaspopenaidocumentation.documentation.audience.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pichincha.cdemsaspopenaidocumentation.documentation.audience.domain.AudienceOverride;
import com.pichincha.cdemsaspopenaidocumentation.documentation.audience.domain.AudiencePolicy;
import com.pichincha.cdemsaspopenaidocumentation.documentation.audience.domain.AudienceProfile;
import com.pichincha.cdemsaspopenaidocumentation.documentation.audience.domain.AudienceType;
import com.pichincha.cdemsaspopenaidocumentation.documentation.audience.domain.ResolvedAudienceProfile;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.DocumentTemplate;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateMetadata;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateVersion;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AudienceProfileResolverTest {

  private final AudienceProfileResolver resolver = new AudienceProfileResolver();

  @Test
  void shouldUseDefaultAudienceWhenNoOverrideIsProvided() {
    DocumentTemplate template = template(policy(true));

    ResolvedAudienceProfile resolved = resolver.resolve(template, null);

    assertEquals(AudienceType.OPERATIONS, resolved.resolvedType());
    assertEquals("template-default", resolved.resolutionSource());
  }

  @Test
  void shouldUseRequestedAudienceWhenSupported() {
    DocumentTemplate template = template(policy(true));
    AudienceOverride override = new AudienceOverride(AudienceType.SECURITY,
        "", "Need security view", false);

    ResolvedAudienceProfile resolved = resolver.resolve(template, override);

    assertEquals(AudienceType.SECURITY, resolved.resolvedType());
    assertEquals("request-override", resolved.resolutionSource());
  }

  @Test
  void shouldFallbackWhenAudienceIsUnsupported() {
    AudiencePolicy policy = new AudiencePolicy(AudienceType.OPERATIONS,
        EnumSet.of(AudienceType.OPERATIONS, AudienceType.DEVELOPERS), true,
        AudienceType.OPERATIONS);
    DocumentTemplate template = template(policy);
    AudienceOverride override = new AudienceOverride(AudienceType.SECURITY,
        "", "Need security view", false);

    ResolvedAudienceProfile resolved = resolver.resolve(template, override);

    assertEquals(AudienceType.OPERATIONS, resolved.resolvedType());
    assertTrue(resolved.fallbackApplied());
    assertTrue(resolved.warnings().stream().anyMatch(warning ->
        warning.contains("not supported")));
  }

  @Test
  void shouldRejectOverrideWhenPolicyDisablesIt() {
    DocumentTemplate template = template(policy(false));
    AudienceOverride override = new AudienceOverride(AudienceType.SECURITY,
        "es", "Need security view", false);

    ResolvedAudienceProfile resolved = resolver.resolve(template, override);

    assertEquals(AudienceType.OPERATIONS, resolved.resolvedType());
    assertEquals("fallback-policy", resolved.resolutionSource());
    assertEquals("es", resolved.profile().preferredLanguage());
  }

  private DocumentTemplate template(AudiencePolicy policy) {
    Map<AudienceType, AudienceProfile> profiles = new EnumMap<>(AudienceType.class);
    profiles.putAll(AudienceProfile.defaults());
    return new DocumentTemplate("error-manual", "Error Manual",
        new TemplateVersion("1.0.0"), List.of(), TemplateMetadata.internal(),
        policy, profiles);
  }

  private AudiencePolicy policy(boolean overrideAllowed) {
    return new AudiencePolicy(AudienceType.OPERATIONS,
        EnumSet.allOf(AudienceType.class), overrideAllowed, AudienceType.OPERATIONS);
  }
}
