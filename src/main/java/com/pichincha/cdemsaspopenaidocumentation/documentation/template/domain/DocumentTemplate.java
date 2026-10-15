package com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain;

import com.pichincha.cdemsaspopenaidocumentation.documentation.audience.domain
  .AudiencePolicy;
import com.pichincha.cdemsaspopenaidocumentation.documentation.audience.domain
  .AudienceProfile;
import com.pichincha.cdemsaspopenaidocumentation.documentation.audience.domain
  .AudienceType;
import com.pichincha.cdemsaspopenaidocumentation.documentation.communication.domain
  .CommunicationProfile;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public record DocumentTemplate(
    String id,
    String name,
    TemplateVersion version,
    List<TemplateSection> sections,
    TemplateMetadata metadata,
    AudiencePolicy audiencePolicy,
    Map<AudienceType, AudienceProfile> audienceProfiles,
    CommunicationProfile defaultCommunicationProfile,
    Map<AudienceType, CommunicationProfile> communicationProfilesByAudience) {

  public DocumentTemplate(String id, String name, TemplateVersion version,
      List<TemplateSection> sections, TemplateMetadata metadata) {
    this(id, name, version, sections, metadata, AudiencePolicy.defaultPolicy(),
        AudienceProfile.defaults(), CommunicationProfile.defaultProfile(),
        CommunicationProfile.defaultsByAudience());
  }

  public DocumentTemplate(String id, String name, TemplateVersion version,
      List<TemplateSection> sections, TemplateMetadata metadata,
      AudiencePolicy audiencePolicy,
      Map<AudienceType, AudienceProfile> audienceProfiles) {
    this(id, name, version, sections, metadata, audiencePolicy, audienceProfiles,
        CommunicationProfile.defaultProfile(),
        CommunicationProfile.defaultsByAudience());
  }

  public DocumentTemplate {
    id = StringUtils.defaultString(id).trim();
    name = StringUtils.defaultString(name).trim();
    version = version == null ? TemplateVersion.initial() : version;
    sections = normalizeSections(sections);
    metadata = metadata == null ? TemplateMetadata.internal() : metadata;
    audiencePolicy = audiencePolicy == null ? AudiencePolicy.defaultPolicy()
      : audiencePolicy;
    audienceProfiles = normalizeProfiles(audienceProfiles, audiencePolicy);
    defaultCommunicationProfile = defaultCommunicationProfile == null
      ? CommunicationProfile.defaultProfile() : defaultCommunicationProfile;
    communicationProfilesByAudience = normalizeCommunicationProfiles(
      communicationProfilesByAudience, defaultCommunicationProfile,
      audiencePolicy);
  }

  private static List<TemplateSection> normalizeSections(List<TemplateSection> sections) {
    if (sections == null || sections.isEmpty()) {
      return List.of();
    }
    return sections.stream().filter(section -> section != null).distinct()
        .sorted(Comparator.comparingInt(TemplateSection::order)
            .thenComparing(TemplateSection::id)).toList();
  }

  private static Map<AudienceType, AudienceProfile> normalizeProfiles(
      Map<AudienceType, AudienceProfile> profiles, AudiencePolicy policy) {
    Map<AudienceType, AudienceProfile> merged = new EnumMap<>(AudienceType.class);
    merged.putAll(AudienceProfile.defaults());
    if (profiles != null && !profiles.isEmpty()) {
      merged.putAll(profiles);
    }
    policy.supportedAudiences().forEach(audienceType -> merged.putIfAbsent(audienceType,
        AudienceProfile.defaults().get(audienceType)));
    return Map.copyOf(merged);
  }

  private static Map<AudienceType, CommunicationProfile>
      normalizeCommunicationProfiles(
      Map<AudienceType, CommunicationProfile> profiles,
      CommunicationProfile defaultProfile,
      AudiencePolicy policy) {
    Map<AudienceType, CommunicationProfile> merged =
        new EnumMap<>(AudienceType.class);
    merged.putAll(CommunicationProfile.defaultsByAudience());
    if (profiles != null && !profiles.isEmpty()) {
      merged.putAll(profiles);
    }
    policy.supportedAudiences().forEach(audienceType -> merged.putIfAbsent(
        audienceType, defaultProfile));
    return Map.copyOf(merged);
  }
}
