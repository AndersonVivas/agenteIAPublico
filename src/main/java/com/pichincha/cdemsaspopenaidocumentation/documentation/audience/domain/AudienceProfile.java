package com.pichincha.cdemsaspopenaidocumentation.documentation.audience.domain;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public record AudienceProfile(
    AudienceType audienceType,
    String displayName,
    String preferredLanguage,
    String businessContext,
    String decisionFocus,
    List<String> primaryConcerns,
    List<String> excludedConcerns,
    String preferredEvidenceVisibility) {

  public AudienceProfile {
    audienceType = audienceType == null ? AudienceType.OPERATIONS : audienceType;
    displayName = normalize(displayName);
    preferredLanguage = normalize(preferredLanguage);
    businessContext = normalize(businessContext);
    decisionFocus = normalize(decisionFocus);
    primaryConcerns = normalizeList(primaryConcerns);
    excludedConcerns = normalizeList(excludedConcerns);
    preferredEvidenceVisibility = normalize(preferredEvidenceVisibility);
  }

  public static Map<AudienceType, AudienceProfile> defaults() {
    Map<AudienceType, AudienceProfile> defaults = new EnumMap<>(AudienceType.class);
    defaults.put(AudienceType.OPERATIONS, operations());
    defaults.put(AudienceType.DEVELOPERS, developers());
    defaults.put(AudienceType.ARCHITECTS, architects());
    defaults.put(AudienceType.FUNCTIONAL_USERS, functionalUsers());
    defaults.put(AudienceType.SRE, sre());
    defaults.put(AudienceType.SECURITY, security());
    return Map.copyOf(defaults);
  }

  private static AudienceProfile operations() {
    return new AudienceProfile(AudienceType.OPERATIONS, "Operations", "es",
        "Daily operation", "Mitigate incidents", List.of("Symptoms", "Actions"),
        List.of("Code internals"), "summarized");
  }

  private static AudienceProfile developers() {
    return new AudienceProfile(AudienceType.DEVELOPERS, "Developers", "en",
        "Implementation", "Fix root causes", List.of("Code", "Configuration"),
        List.of("Business-only narrative"), "explicit");
  }

  private static AudienceProfile architects() {
    return new AudienceProfile(AudienceType.ARCHITECTS, "Architects", "en",
        "Architecture governance", "Manage technical risk",
        List.of("Resilience", "Technical debt"), List.of("Tactical steps"),
        "explicit");
  }

  private static AudienceProfile functionalUsers() {
    return new AudienceProfile(AudienceType.FUNCTIONAL_USERS, "Functional Users",
        "es", "Business process", "Assess process impact",
        List.of("Business impact", "User effects"),
        List.of("Framework internals"), "translated");
  }

  private static AudienceProfile sre() {
    return new AudienceProfile(AudienceType.SRE, "SRE", "en",
        "Reliability engineering", "Recover service reliability",
        List.of("Observability", "Recovery"), List.of("Business-only details"),
        "explicit");
  }

  private static AudienceProfile security() {
    return new AudienceProfile(AudienceType.SECURITY, "Security", "en",
        "Security posture", "Reduce exposure", List.of("Controls", "Risk"),
        List.of("Unrelated implementation detail"), "explicit");
  }

  private static String normalize(String value) {
    return StringUtils.defaultString(value).trim();
  }

  private static List<String> normalizeList(List<String> values) {
    if (values == null || values.isEmpty()) {
      return List.of();
    }
    return values.stream().map(AudienceProfile::normalize)
        .filter(StringUtils::isNotBlank).toList();
  }
}
