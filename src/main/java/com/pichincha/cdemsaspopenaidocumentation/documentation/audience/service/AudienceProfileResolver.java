package com.pichincha.cdemsaspopenaidocumentation.documentation.audience.service;

import com.pichincha.cdemsaspopenaidocumentation.documentation.audience.domain.AudienceOverride;
import com.pichincha.cdemsaspopenaidocumentation.documentation.audience.domain.AudiencePolicy;
import com.pichincha.cdemsaspopenaidocumentation.documentation.audience.domain.AudienceProfile;
import com.pichincha.cdemsaspopenaidocumentation.documentation.audience.domain.AudienceType;
import com.pichincha.cdemsaspopenaidocumentation.documentation.audience.domain.ResolvedAudienceProfile;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.DocumentTemplate;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.port.TemplateRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class AudienceProfileResolver {

  public ResolvedAudienceProfile resolve(DocumentTemplate template,
      AudienceOverride audienceOverride) {
    AudiencePolicy policy = template.audiencePolicy();
    Map<AudienceType, AudienceProfile> profiles = template.audienceProfiles();
    List<String> warnings = new ArrayList<>();
    AudienceType resolvedType = resolvedType(policy, audienceOverride, warnings);
    AudienceProfile profile = profiles.getOrDefault(resolvedType,
        AudienceProfile.defaults().get(resolvedType));
    String source = source(policy, audienceOverride, resolvedType);
    boolean fallback = source.equals("fallback-policy");
    return new ResolvedAudienceProfile(applyLanguageOverride(profile, audienceOverride),
        resolvedType, source, fallback, warnings);
  }

  public Optional<ResolvedAudienceProfile> resolveFromRepository(
      TemplateRepository templateRepository, String templateId,
      AudienceOverride audienceOverride) {
    return templateRepository.resolveActive(templateId).map(result ->
        resolve(result.template(), audienceOverride));
  }

  private AudienceType resolvedType(AudiencePolicy policy,
      AudienceOverride audienceOverride, List<String> warnings) {
    if (audienceOverride == null || !audienceOverride.hasRequestedAudience()) {
      return policy.defaultAudience();
    }
    if (!policy.overrideAllowed() && !audienceOverride.forceIfSupported()) {
      warnings.add("Audience override is disabled by policy.");
      return policy.defaultAudience();
    }
    if (policy.supports(audienceOverride.requestedAudience())) {
      return audienceOverride.requestedAudience();
    }
    warnings.add("Requested audience is not supported. Applying fallback.");
    return policy.fallbackAudience();
  }

  private String source(AudiencePolicy policy, AudienceOverride audienceOverride,
      AudienceType resolvedType) {
    if (audienceOverride == null || !audienceOverride.hasRequestedAudience()) {
      return "template-default";
    }
    if (resolvedType == audienceOverride.requestedAudience()) {
      return "request-override";
    }
    return "fallback-policy";
  }

  private AudienceProfile applyLanguageOverride(AudienceProfile profile,
      AudienceOverride override) {
    if (override == null || override.preferredLanguageOverride().isBlank()) {
      return profile;
    }
    return new AudienceProfile(profile.audienceType(), profile.displayName(),
        override.preferredLanguageOverride(), profile.businessContext(),
        profile.decisionFocus(), profile.primaryConcerns(),
        profile.excludedConcerns(), profile.preferredEvidenceVisibility());
  }
}
