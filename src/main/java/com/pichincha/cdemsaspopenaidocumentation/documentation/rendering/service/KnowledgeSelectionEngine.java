package com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.service;

import com.pichincha.cdemsaspopenaidocumentation.documentation.audience.domain.AudienceProfile;
import com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.domain.SelectedKnowledge;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.DocumentTemplate;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.KnowledgeFinding;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.ProjectKnowledgeBase;
import java.util.List;
import java.util.Locale;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class KnowledgeSelectionEngine {

  public SelectedKnowledge select(ProjectKnowledgeBase knowledgeBase,
      DocumentTemplate template) {
    if (knowledgeBase == null) {
      return new SelectedKnowledge(List.of(), List.of(), List.of(),
          List.of("Knowledge base is empty."));
    }
    List<KnowledgeFinding> selected = knowledgeBase.allFindings().stream()
        .filter(finding -> isRelevant(template, template.reader().audience(), finding))
        .toList();
    return new SelectedKnowledge(selected, knowledgeBase.knowledgeGaps(),
        knowledgeBase.knowledgeConflicts(), List.of());
  }

  private boolean isRelevant(DocumentTemplate template, AudienceProfile audience,
      KnowledgeFinding finding) {
    if (isApiFocused(template) && !isApiOrDependency(finding)) {
      return false;
    }
    if (audience.excludedConcerns().isEmpty()) {
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
