package com.pichincha.cdemsaspopenaidocumentation.copilot.support;

import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentationAgentType;
import com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.domain.SectionContext;
import java.util.List;
import java.util.Locale;

/**
 * Maps a {@code DocumentationAgentType} to the template sections it is relevant to, so
 * every agent only receives the writing instructions it actually needs. Falls back to the
 * full section list when the template does not name the category explicitly, so an agent
 * is never left without structural guidance.
 */
public final class SectionContextCategoryMatcher {

  private SectionContextCategoryMatcher() {
  }

  public static List<SectionContext> match(DocumentationAgentType agentType,
      List<SectionContext> sections) {
    if (sections == null || sections.isEmpty()) {
      return List.of();
    }
    List<SectionContext> matched = sections.stream()
        .filter(section -> matches(agentType, section)).toList();
    return matched.isEmpty() ? sections : matched;
  }

  private static boolean matches(DocumentationAgentType agentType, SectionContext section) {
    String searchable = (section.title() + " " + section.objective() + " "
        + section.narrativeIntent()).toLowerCase(Locale.ROOT);
    return switch (agentType) {
      case API -> searchable.contains("api") || searchable.contains("endpoint");
      case DEPENDENCY -> searchable.contains("depend") || searchable.contains("integra");
      case CONFIGURATION -> searchable.contains("config") || searchable.contains("timeout");
      case MONITORING -> searchable.contains("monitor") || searchable.contains("alert")
          || searchable.contains("observ");
      case EXCEPTION -> searchable.contains("exception") || searchable.contains("error")
          || searchable.contains("falla");
      case BUSINESS_FLOW -> searchable.contains("business") || searchable.contains("negocio")
          || searchable.contains("proceso") || searchable.contains("flujo")
          || searchable.contains("regla");
    };
  }
}
