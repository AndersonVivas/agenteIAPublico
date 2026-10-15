package com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.service;

import com.pichincha.cdemsaspopenaidocumentation.documentation.communication.domain.CommunicationProfile;
import com.pichincha.cdemsaspopenaidocumentation.documentation.communication.domain.PresentationMode;
import com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.domain.AdaptedFinding;
import com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.domain.AdaptedKnowledge;
import com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.domain.SectionContext;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.DocumentTemplate;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateSection;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.EvidenceRef;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class SectionPlanningEngine {

  public List<SectionContext> plan(DocumentTemplate template,
      AdaptedKnowledge adaptedKnowledge, CommunicationProfile communicationProfile) {
    List<TemplateSection> sections = template.sections().isEmpty()
        ? List.of(defaultSection()) : template.sections();
    return sections.stream().map(section -> planSection(section, adaptedKnowledge,
        communicationProfile.presentationMode())).toList();
  }

  private SectionContext planSection(TemplateSection section,
      AdaptedKnowledge adaptedKnowledge, PresentationMode mode) {
    List<AdaptedFinding> matches = adaptedKnowledge.findings().stream()
        .filter(finding -> matchesSection(section, finding)).toList();
    List<String> highlights = matches.stream().map(AdaptedFinding::adaptedSummary).toList();
    List<EvidenceRef> refs = matches.stream().flatMap(f -> f.evidenceRefs().stream())
        .distinct().toList();
    List<String> warnings = new ArrayList<>();
    if (section.required() && matches.isEmpty()) {
      warnings.add("No findings mapped to required section.");
    }
    String empty = section.required() && matches.isEmpty()
        ? "No evidenciado en el escaneo." : "";
    return new SectionContext(section.id(), section.title(), section.objective(),
        section.required(), section.order(), section.objective(), mode, highlights,
        refs, warnings, empty);
  }

  private boolean matchesSection(TemplateSection section, AdaptedFinding finding) {
    String searchable = (section.title() + " " + section.objective())
        .toLowerCase(Locale.ROOT);
    if (searchable.contains("api")) {
      return "api".equalsIgnoreCase(finding.category());
    }
    if (searchable.contains("depend")) {
      return "dependency".equalsIgnoreCase(finding.category());
    }
    if (searchable.contains("config")) {
      return "configuration".equalsIgnoreCase(finding.category());
    }
    if (searchable.contains("monitor")) {
      return "monitoring".equalsIgnoreCase(finding.category());
    }
    if (searchable.contains("exception") || searchable.contains("error")) {
      return "exception".equalsIgnoreCase(finding.category());
    }
    return true;
  }

  private TemplateSection defaultSection() {
    return new TemplateSection("general-overview", "General Overview",
        "Summarize relevant findings", List.of(), true, 1);
  }
}
