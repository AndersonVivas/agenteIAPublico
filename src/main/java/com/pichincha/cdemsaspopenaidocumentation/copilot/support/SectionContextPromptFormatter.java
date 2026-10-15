package com.pichincha.cdemsaspopenaidocumentation.copilot.support;

import com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.domain.SectionContext;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 * Renders the full substance of a {@code SectionContext} (objective, narrative intent,
 * presentation mode, highlights, traceability warnings) instead of just its title, so
 * every prompt that consumes a rendering plan can honor the template's intent end to end.
 */
public final class SectionContextPromptFormatter {

  private SectionContextPromptFormatter() {
  }

  public static String format(List<SectionContext> sections) {
    if (sections == null || sections.isEmpty()) {
      return "- No hay secciones de la plantilla asociadas a esta categoria.";
    }
    StringBuilder builder = new StringBuilder();
    for (SectionContext section : sections) {
      builder.append(formatOne(section)).append('\n');
    }
    return builder.toString().trim();
  }

  private static String formatOne(SectionContext section) {
    StringBuilder builder = new StringBuilder();
    builder.append("- [").append(section.sectionId()).append("] ").append(section.title())
        .append(" | objetivo: ").append(section.objective())
        .append(" | intencion narrativa: ").append(section.narrativeIntent())
        .append(" | obligatoria: ").append(section.required())
        .append(" | orden: ").append(section.order())
        .append(" | modo de presentacion: ").append(section.presentationMode());
    if (!section.highlights().isEmpty()) {
      builder.append("\n  pistas conocidas: ").append(String.join(" ; ", section.highlights()));
    }
    if (!section.warnings().isEmpty()) {
      builder.append("\n  advertencias: ").append(String.join(" ; ", section.warnings()));
    }
    if (StringUtils.isNotBlank(section.emptyStateMessage())) {
      builder.append("\n  mensaje si no hay evidencia: ").append(section.emptyStateMessage());
    }
    return builder.toString();
  }
}
