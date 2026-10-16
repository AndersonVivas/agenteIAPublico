package com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.service;

import com.pichincha.cdemsaspopenaidocumentation.documentation.communication.domain.PresentationMode;
import com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.domain.SectionContext;
import com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.domain.SelectedKnowledge;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.DocumentTemplate;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateSection;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.EvidenceRef;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.KnowledgeFinding;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class SectionPlanningEngine {

  private static final String MISSING_FINDINGS_WARNING =
      "No findings mapped to required section.";

  public List<SectionContext> plan(DocumentTemplate template,
      SelectedKnowledge selectedKnowledge) {
    List<TemplateSection> sections = template.sections().isEmpty()
        ? List.of(defaultSection()) : template.sections();
    PresentationMode mode = template.reader().communication().presentationMode();
    return sections.stream().map(section -> planSection(section, selectedKnowledge, mode,
        template.guidance().missingDataMessage())).toList();
  }

  private SectionContext planSection(TemplateSection section,
      SelectedKnowledge selectedKnowledge, PresentationMode mode, String missingDataMessage) {
    List<KnowledgeFinding> matches = selectedKnowledge.findings().stream()
        .filter(finding -> matchesSection(section, finding)).toList();
    List<String> highlights = matches.stream().map(KnowledgeFinding::summary).toList();
    List<EvidenceRef> refs = matches.stream().flatMap(f -> f.evidenceRefs().stream())
        .distinct().toList();
    boolean unsupported = section.required() && matches.isEmpty();
    return new SectionContext(section.id(), section.title(), section.objective(),
        section.instructions(), section.required(), section.order(), section.objective(), mode,
        highlights, refs, unsupported ? List.of(MISSING_FINDINGS_WARNING) : List.of(),
        unsupported ? missingDataMessage : "");
  }

  private boolean matchesSection(TemplateSection section, KnowledgeFinding finding) {
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
