package com.pichincha.cdemsaspopenaidocumentation.documentation.communication.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.pichincha.cdemsaspopenaidocumentation.documentation.audience.domain.AudienceProfile;
import com.pichincha.cdemsaspopenaidocumentation.documentation.audience.domain.AudienceType;
import com.pichincha.cdemsaspopenaidocumentation.documentation.communication.domain.CommunicationProfile;
import com.pichincha.cdemsaspopenaidocumentation.documentation.communication.domain.NarrativeStyle;
import com.pichincha.cdemsaspopenaidocumentation.documentation.communication.domain.TechnicalAbstractionLevel;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.DocumentTemplate;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateMetadata;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateVersion;
import java.util.List;
import org.junit.jupiter.api.Test;

class CommunicationProfileResolverTest {

  private final CommunicationProfileResolver resolver =
      new CommunicationProfileResolver();

  @Test
  void shouldResolveOperationalCommunicationForOperations() {
    CommunicationProfile profile = resolver.resolve(template(),
        AudienceProfile.defaults().get(AudienceType.OPERATIONS));

    assertEquals(NarrativeStyle.OPERATIONAL, profile.narrativeStyle());
    assertEquals(TechnicalAbstractionLevel.BALANCED,
        profile.technicalAbstractionLevel());
  }

  @Test
  void shouldResolveBusinessImpactCommunicationForFunctionalUsers() {
    CommunicationProfile profile = resolver.resolve(template(),
        AudienceProfile.defaults().get(AudienceType.FUNCTIONAL_USERS));

    assertEquals(NarrativeStyle.BUSINESS_IMPACT, profile.narrativeStyle());
    assertEquals(TechnicalAbstractionLevel.TRANSLATED,
        profile.technicalAbstractionLevel());
  }

  @Test
  void shouldResolveImplementationCommunicationForDevelopers() {
    CommunicationProfile profile = resolver.resolve(template(),
        AudienceProfile.defaults().get(AudienceType.DEVELOPERS));

    assertEquals(NarrativeStyle.IMPLEMENTATION_GUIDANCE,
        profile.narrativeStyle());
    assertEquals(TechnicalAbstractionLevel.EXPLICIT,
        profile.technicalAbstractionLevel());
  }

  @Test
  void shouldResolveArchitectureCommunicationForArchitects() {
    CommunicationProfile profile = resolver.resolve(template(),
        AudienceProfile.defaults().get(AudienceType.ARCHITECTS));

    assertEquals(NarrativeStyle.ARCHITECTURAL_ASSESSMENT,
        profile.narrativeStyle());
    assertEquals(TechnicalAbstractionLevel.BALANCED,
        profile.technicalAbstractionLevel());
  }

  @Test
  void shouldUseAudienceLanguageWhenAvailable() {
    AudienceProfile audience = new AudienceProfile(AudienceType.DEVELOPERS,
        "Developers", "pt", "Implementation", "Fix root causes",
        List.of("Code"), List.of(), "explicit");

    CommunicationProfile profile = resolver.resolve(template(), audience);

    assertEquals("pt", profile.defaultLanguage());
  }

  private DocumentTemplate template() {
    return new DocumentTemplate("error-manual", "Error Manual",
        new TemplateVersion("1.0.0"), List.of(), TemplateMetadata.internal());
  }
}
