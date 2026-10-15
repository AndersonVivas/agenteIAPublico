package com.pichincha.cdemsaspopenaidocumentation.documentation.communication.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pichincha.cdemsaspopenaidocumentation.documentation.audience.domain.AudienceType;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CommunicationProfileTest {

  @Test
  void shouldExposeDefaultsForAllAudiences() {
    Map<AudienceType, CommunicationProfile> defaults =
        CommunicationProfile.defaultsByAudience();

    assertEquals(6, defaults.size());
    assertEquals(NarrativeStyle.OPERATIONAL,
        defaults.get(AudienceType.OPERATIONS).narrativeStyle());
    assertEquals(NarrativeStyle.BUSINESS_IMPACT,
        defaults.get(AudienceType.FUNCTIONAL_USERS).narrativeStyle());
  }

  @Test
  void shouldNormalizeTermsAndLanguage() {
    CommunicationProfile profile = new CommunicationProfile(" profile ",
        NarrativeStyle.OPERATIONAL, PresentationMode.STEP_BY_STEP,
        ReadingLevel.OPERATIONAL, TechnicalAbstractionLevel.BALANCED,
        TerminologyPolicy.TRANSLATED_TECHNICAL, " es ",
        java.util.List.of(" impact ", " "), java.util.List.of(" tech ", ""));

    assertEquals("profile", profile.profileId());
    assertEquals("es", profile.defaultLanguage());
    assertEquals(1, profile.preferredTerms().size());
    assertTrue(profile.forbiddenTerms().contains("tech"));
  }
}
