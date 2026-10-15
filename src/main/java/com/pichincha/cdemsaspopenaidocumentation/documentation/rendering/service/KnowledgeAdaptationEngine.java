package com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.service;

import com.pichincha.cdemsaspopenaidocumentation.documentation.audience.domain.AudienceProfile;
import com.pichincha.cdemsaspopenaidocumentation.documentation.communication.domain.CommunicationProfile;
import com.pichincha.cdemsaspopenaidocumentation.documentation.communication.domain.NarrativeStyle;
import com.pichincha.cdemsaspopenaidocumentation.documentation.communication.domain.TechnicalAbstractionLevel;
import com.pichincha.cdemsaspopenaidocumentation.documentation.communication.domain.TerminologyPolicy;
import com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.domain.AdaptedFinding;
import com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.domain.AdaptedKnowledge;
import com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.domain.SelectedKnowledge;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.KnowledgeFinding;
import java.util.List;
import java.util.Locale;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class KnowledgeAdaptationEngine {

  public AdaptedKnowledge adapt(SelectedKnowledge selected,
      AudienceProfile audienceProfile, CommunicationProfile communicationProfile) {
    List<AdaptedFinding> adaptedFindings = selected.findings().stream()
        .map(finding -> adaptFinding(finding, communicationProfile))
        .toList();
    return new AdaptedKnowledge(adaptedFindings, selected.gaps(), selected.conflicts(),
        selected.warnings());
  }

  private AdaptedFinding adaptFinding(KnowledgeFinding finding,
      CommunicationProfile communicationProfile) {
    String summary = prefixedSummary(finding.summary(), communicationProfile.narrativeStyle());
    summary = byAbstraction(summary, communicationProfile.technicalAbstractionLevel());
    summary = byTerminology(summary, communicationProfile.terminologyPolicy());
    return new AdaptedFinding(finding.findingId(), finding.category(), finding.severity(),
        finding.title(), summary, finding.evidenceRefs());
  }

  private String prefixedSummary(String summary, NarrativeStyle style) {
    String text = StringUtils.defaultString(summary).trim();
    if (style == NarrativeStyle.OPERATIONAL) {
      return "Operational impact: " + text;
    }
    if (style == NarrativeStyle.BUSINESS_IMPACT) {
      return "Business process impact: " + text;
    }
    if (style == NarrativeStyle.IMPLEMENTATION_GUIDANCE) {
      return "Implementation guidance: " + text;
    }
    if (style == NarrativeStyle.ARCHITECTURAL_ASSESSMENT) {
      return "Architecture risk: " + text;
    }
    if (style == NarrativeStyle.DIAGNOSTIC) {
      return "Diagnostic focus: " + text;
    }
    return "Audit signal: " + text;
  }

  private String byAbstraction(String summary, TechnicalAbstractionLevel level) {
    if (level == TechnicalAbstractionLevel.HIDDEN) {
      return hideTerms(summary);
    }
    if (level == TechnicalAbstractionLevel.TRANSLATED) {
      return translateTerms(summary);
    }
    if (level == TechnicalAbstractionLevel.BALANCED) {
      return summary + " Technical details are available in traceability.";
    }
    return summary;
  }

  private String byTerminology(String summary, TerminologyPolicy policy) {
    if (policy == TerminologyPolicy.BUSINESS_FRIENDLY) {
      return translateTerms(summary);
    }
    if (policy == TerminologyPolicy.TRANSLATED_TECHNICAL) {
      return translateTerms(summary);
    }
    if (policy == TerminologyPolicy.MIXED) {
      return summary + " (mixed-terminology view)";
    }
    return summary;
  }

  private String hideTerms(String summary) {
    String updated = summary;
    updated = updated.replaceAll("(?i)feignclient", "external integration");
    updated = updated.replaceAll("(?i)handler", "error management logic");
    return updated;
  }

  private String translateTerms(String summary) {
    String translated = hideTerms(summary);
    translated = translated.replaceAll("(?i)timeout", "response time limit");
    translated = translated.replaceAll("(?i)throws|(?i)catches", "error control");
    return translated.toLowerCase(Locale.ROOT).startsWith("business")
        ? StringUtils.capitalize(translated) : translated;
  }
}
