package com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.service;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pichincha.cdemsaspopenaidocumentation.documentation.audience.domain.AudienceProfile;
import com.pichincha.cdemsaspopenaidocumentation.documentation.audience.domain.AudienceType;
import com.pichincha.cdemsaspopenaidocumentation.documentation.communication.domain.CommunicationProfile;
import com.pichincha.cdemsaspopenaidocumentation.documentation.communication.domain.NarrativeStyle;
import com.pichincha.cdemsaspopenaidocumentation.documentation.communication.domain.PresentationMode;
import com.pichincha.cdemsaspopenaidocumentation.documentation.communication.domain.ReadingLevel;
import com.pichincha.cdemsaspopenaidocumentation.documentation.communication.domain.TechnicalAbstractionLevel;
import com.pichincha.cdemsaspopenaidocumentation.documentation.communication.domain.TerminologyPolicy;
import com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.domain.AdaptedKnowledge;
import com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.domain.SelectedKnowledge;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.EvidenceRef;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.KnowledgeFinding;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class KnowledgeAdaptationEngineTest {

  private final KnowledgeAdaptationEngine engine = new KnowledgeAdaptationEngine();

  @Test
  void shouldTranslateTechnicalTermsForBusinessFriendlyProfile() {
    SelectedKnowledge selected = new SelectedKnowledge(List.of(finding()), List.of(),
        List.of(), List.of(), Map.of(), List.of());
    CommunicationProfile profile = new CommunicationProfile("functional",
        NarrativeStyle.BUSINESS_IMPACT, PresentationMode.SUMMARY_FIRST,
        ReadingLevel.FUNCTIONAL, TechnicalAbstractionLevel.TRANSLATED,
        TerminologyPolicy.BUSINESS_FRIENDLY, "es", List.of(), List.of());

    AdaptedKnowledge adapted = engine.adapt(selected,
        AudienceProfile.defaults().get(AudienceType.FUNCTIONAL_USERS), profile);

    String summary = adapted.findings().get(0).adaptedSummary();
    assertTrue(summary.contains("Business process impact:"));
    assertTrue(summary.contains("response time limit"));
    assertTrue(summary.contains("external integration"));
  }

  private KnowledgeFinding finding() {
    return new KnowledgeFinding("f-1", "api", "api-timeout", "Timeout issue",
        "FeignClient timeout throws alert", "high", 0.9,
        List.of(new EvidenceRef("e-1", "src/A.java", "RULE", "signal", 0.9)));
  }
}
