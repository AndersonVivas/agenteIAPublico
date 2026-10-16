package com.pichincha.cdemsaspopenaidocumentation.documentation.communication.domain;

import com.pichincha.cdemsaspopenaidocumentation.documentation.audience.domain.AudienceType;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public record CommunicationProfile(
    String profileId,
    NarrativeStyle narrativeStyle,
    PresentationMode presentationMode,
    ReadingLevel readingLevel,
    TechnicalAbstractionLevel technicalAbstractionLevel,
    TerminologyPolicy terminologyPolicy,
    String defaultLanguage,
    List<String> preferredTerms,
    List<String> forbiddenTerms) {

  public CommunicationProfile {
    profileId = StringUtils.defaultIfBlank(profileId, "default").trim();
    narrativeStyle = narrativeStyle == null ? NarrativeStyle.OPERATIONAL
        : narrativeStyle;
    presentationMode = presentationMode == null ? PresentationMode.STEP_BY_STEP
        : presentationMode;
    readingLevel = readingLevel == null ? ReadingLevel.OPERATIONAL : readingLevel;
    technicalAbstractionLevel = technicalAbstractionLevel == null
        ? TechnicalAbstractionLevel.BALANCED : technicalAbstractionLevel;
    terminologyPolicy = terminologyPolicy == null
        ? TerminologyPolicy.TRANSLATED_TECHNICAL : terminologyPolicy;
    defaultLanguage = StringUtils.defaultIfBlank(defaultLanguage, "es").trim();
    preferredTerms = normalizeTerms(preferredTerms);
    forbiddenTerms = normalizeTerms(forbiddenTerms);
  }

  public static Map<AudienceType, CommunicationProfile> defaultsByAudience() {
    return Map.of(AudienceType.OPERATIONS, operations(),
        AudienceType.FUNCTIONAL_USERS, functionalUsers(),
        AudienceType.DEVELOPERS, developers(),
        AudienceType.ARCHITECTS, architects(),
        AudienceType.SRE, sre(),
        AudienceType.SECURITY, security());
  }

  private static CommunicationProfile operations() {
    return new CommunicationProfile("operations-default", NarrativeStyle.OPERATIONAL,
        PresentationMode.STEP_BY_STEP, ReadingLevel.OPERATIONAL,
        TechnicalAbstractionLevel.BALANCED, TerminologyPolicy.TRANSLATED_TECHNICAL, "es",
        List.of("impact", "symptom", "action"),
        List.of("throws", "catches", "handler internals"));
  }

  private static CommunicationProfile functionalUsers() {
    return new CommunicationProfile("functional-default", NarrativeStyle.BUSINESS_IMPACT,
        PresentationMode.SUMMARY_FIRST, ReadingLevel.FUNCTIONAL,
        TechnicalAbstractionLevel.TRANSLATED, TerminologyPolicy.BUSINESS_FRIENDLY, "es",
        List.of("business process", "user impact", "service disruption"),
        List.of("FeignClient", "handler", "stack trace"));
  }

  private static CommunicationProfile developers() {
    return new CommunicationProfile("developers-default",
        NarrativeStyle.IMPLEMENTATION_GUIDANCE, PresentationMode.CATALOG,
        ReadingLevel.TECHNICAL, TechnicalAbstractionLevel.EXPLICIT,
        TerminologyPolicy.ENGINEERING_PRECISE, "en",
        List.of("timeout", "configuration", "implementation"),
        List.of("business-only narrative"));
  }

  private static CommunicationProfile architects() {
    return new CommunicationProfile("architects-default",
        NarrativeStyle.ARCHITECTURAL_ASSESSMENT, PresentationMode.RISK_MATRIX,
        ReadingLevel.EXPERT, TechnicalAbstractionLevel.BALANCED, TerminologyPolicy.MIXED, "en",
        List.of("resilience", "technical debt", "cross-cutting risk"),
        List.of("step-by-step operator script"));
  }

  private static CommunicationProfile sre() {
    return new CommunicationProfile("sre-default", NarrativeStyle.DIAGNOSTIC,
        PresentationMode.STEP_BY_STEP, ReadingLevel.EXPERT,
        TechnicalAbstractionLevel.EXPLICIT, TerminologyPolicy.TRANSLATED_TECHNICAL, "en",
        List.of("reliability", "recovery", "observability"),
        List.of("business-only framing"));
  }

  private static CommunicationProfile security() {
    return new CommunicationProfile("security-default", NarrativeStyle.AUDIT,
        PresentationMode.RISK_MATRIX, ReadingLevel.EXPERT,
        TechnicalAbstractionLevel.EXPLICIT, TerminologyPolicy.ENGINEERING_PRECISE, "en",
        List.of("exposure", "control", "risk"), List.of("non-security detail"));
  }

  private static List<String> normalizeTerms(List<String> values) {
    if (values == null || values.isEmpty()) {
      return List.of();
    }
    return values.stream().map(StringUtils::defaultString).map(String::trim)
        .filter(StringUtils::isNotBlank).toList();
  }
}
