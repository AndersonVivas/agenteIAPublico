package com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.service;

import com.pichincha.cdemsaspopenaidocumentation.documentation.audience.domain.AudienceProfile;
import com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.domain.SelectedKnowledge;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.DocumentTemplate;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.KnowledgeFinding;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.ProjectKnowledgeBase;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class KnowledgeSelectionEngine {

  public SelectedKnowledge select(ProjectKnowledgeBase knowledgeBase,
      DocumentTemplate template, AudienceProfile audienceProfile) {
    if (knowledgeBase == null) {
      return new SelectedKnowledge(List.of(), List.of(), List.of(), List.of(), Map.of(),
          List.of("Knowledge base is empty."));
    }
    List<KnowledgeFinding> selected = new ArrayList<>();
    List<String> excluded = new ArrayList<>();
    Map<String, String> reasons = new LinkedHashMap<>();
    for (KnowledgeFinding finding : knowledgeBase.allFindings()) {
      if (isRelevant(template, audienceProfile, finding)) {
        selected.add(finding);
        reasons.put(finding.findingId(), "selected-by-template-and-audience");
      } else {
        excluded.add(finding.findingId());
        reasons.put(finding.findingId(), "excluded-by-template-or-audience");
      }
    }
    return new SelectedKnowledge(selected, knowledgeBase.knowledgeGaps(),
        knowledgeBase.knowledgeConflicts(), excluded, reasons, List.of());
  }

  private boolean isRelevant(DocumentTemplate template, AudienceProfile audience,
      KnowledgeFinding finding) {
    if (isApiFocused(template) && !isApiOrDependency(finding)) {
      return false;
    }
    if (audience == null || audience.excludedConcerns().isEmpty()) {
      return true;
    }
    String searchable = searchableText(finding);
    return audience.excludedConcerns().stream().map(this::tokenize)
        .noneMatch(searchable::contains);
  }

  private boolean isApiFocused(DocumentTemplate template) {
    String name = template.name().toLowerCase(Locale.ROOT);
    return name.contains("api");
  }

  private boolean isApiOrDependency(KnowledgeFinding finding) {
    return "api".equalsIgnoreCase(finding.category())
        || "dependency".equalsIgnoreCase(finding.category());
  }

  private String searchableText(KnowledgeFinding finding) {
    return String.join(" ", finding.category(), finding.title(), finding.summary())
        .toLowerCase(Locale.ROOT);
  }

  private String tokenize(String term) {
    return StringUtils.defaultString(term).toLowerCase(Locale.ROOT).trim();
  }
}
