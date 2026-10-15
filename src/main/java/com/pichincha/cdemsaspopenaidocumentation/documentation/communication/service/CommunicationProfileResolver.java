package com.pichincha.cdemsaspopenaidocumentation.documentation.communication.service;

import com.pichincha.cdemsaspopenaidocumentation.documentation.audience.domain.AudienceProfile;
import com.pichincha.cdemsaspopenaidocumentation.documentation.audience.domain.AudienceType;
import com.pichincha.cdemsaspopenaidocumentation.documentation.communication.domain.CommunicationProfile;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.DocumentTemplate;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.port.TemplateRepository;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class CommunicationProfileResolver {

  public CommunicationProfile resolve(DocumentTemplate template,
      AudienceProfile audienceProfile) {
    AudienceType audienceType = audienceProfile == null
        ? template.audiencePolicy().defaultAudience() : audienceProfile.audienceType();
    Map<AudienceType, CommunicationProfile> profiles =
        template.communicationProfilesByAudience();
    CommunicationProfile resolved = profiles.getOrDefault(audienceType,
        template.defaultCommunicationProfile());
    return applyLanguageFromAudience(resolved, audienceProfile);
  }

  public Optional<CommunicationProfile> resolveFromRepository(
      TemplateRepository templateRepository, String templateId,
      AudienceProfile audienceProfile) {
    return templateRepository.resolveActive(templateId).map(result ->
        resolve(result.template(), audienceProfile));
  }

  private CommunicationProfile applyLanguageFromAudience(
      CommunicationProfile profile, AudienceProfile audienceProfile) {
    if (audienceProfile == null || audienceProfile.preferredLanguage().isBlank()) {
      return profile;
    }
    return new CommunicationProfile(profile.profileId(), profile.narrativeStyle(),
        profile.presentationMode(), profile.readingLevel(),
        profile.technicalAbstractionLevel(), profile.terminologyPolicy(),
        audienceProfile.preferredLanguage(), profile.preferredTerms(),
        profile.forbiddenTerms());
  }
}
