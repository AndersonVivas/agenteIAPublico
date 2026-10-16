package com.pichincha.cdemsaspopenaidocumentation.copilot.support;

import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.ChangelogEntry;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Renders the version history of an evolutionary document as a Markdown changelog, newest
 * version first.
 */
public final class ChangelogMarkdownRenderer {

  private static final String NONE = "ninguno";
  private static final String NOT_AVAILABLE = "n/d";

  private ChangelogMarkdownRenderer() {
  }

  public static String render(String documentTitle, List<ChangelogEntry> entriesNewestFirst) {
    StringBuilder builder = new StringBuilder("# Changelog - ").append(documentTitle)
        .append('\n');
    entriesNewestFirst.forEach(entry -> builder.append('\n').append(renderEntry(entry)));
    return builder.toString();
  }

  private static String renderEntry(ChangelogEntry entry) {
    String pullRequest = entry.pullRequestId() == null ? NOT_AVAILABLE
        : "#" + entry.pullRequestId();
    return "## Version " + entry.version() + " - "
        + DateTimeFormatter.ISO_INSTANT.format(entry.createdAt()) + "\n"
        + line("Pull Request", pullRequest)
        + line("Commit", entry.headCommit())
        + line("Alcance del escaneo", entry.scanScope() + " (" + entry.changedFiles()
            + " archivos cambiados)")
        + line("Evidencia", "+" + entry.evidenceAdded() + " / -" + entry.evidenceRemoved())
        + line("Secciones actualizadas", joinOrNone(entry.updatedSections()))
        + line("Advertencias", joinOrNone(entry.warnings()));
  }

  private static String line(String label, String value) {
    return "- " + label + ": " + (value == null ? NOT_AVAILABLE : value) + "\n";
  }

  private static String joinOrNone(List<String> values) {
    return values.isEmpty() ? NONE : String.join("; ", values);
  }
}
