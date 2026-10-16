package com.pichincha.cdemsaspopenaidocumentation.copilot.support;

import com.pichincha.cdemsaspopenaidocumentation.domain.CodeSignalEvidence;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

/**
 * Renders a compact textual view of a partitioned evidence slice for a specialist agent
 * prompt: the matched line plus the surrounding code lines captured by the scanner, so the
 * agent understands when and why the signal happens without reading the repository. The
 * surrounding lines share a fixed character budget to keep prompts bounded.
 */
public final class AgentEvidenceFormatter {

  private static final String NO_EVIDENCE =
      "- No se encontro evidencia para esta categoria en el escaneo estatico.";
  private static final int CONTEXT_BUDGET_CHARS = 40_000;
  private static final int MAX_CONTEXT_LINE_CHARS = 160;
  private static final String CONTEXT_INDENT = "     ";

  private AgentEvidenceFormatter() {
  }

  public static String format(List<CodeSignalEvidence> evidence) {
    if (evidence == null || evidence.isEmpty()) {
      return NO_EVIDENCE;
    }
    StringBuilder builder = new StringBuilder();
    int remainingContext = CONTEXT_BUDGET_CHARS;
    for (int index = 0; index < evidence.size(); index++) {
      String context = remainingContext > 0 ? context(evidence.get(index)) : "";
      remainingContext -= context.length();
      builder.append(formatOne(index + 1, evidence.get(index))).append(context).append('\n');
    }
    return builder.toString().trim();
  }

  private static String formatOne(int position, CodeSignalEvidence item) {
    String location = item.matchLine() > 0
        ? blank(item.filePath()) + ":" + item.matchLine() : blank(item.filePath());
    String tags = item.tags() == null || item.tags().isEmpty()
        ? "" : " | tags:" + String.join(",", item.tags());
    return position + ". [id:" + blank(item.evidenceId()) + "] regla:" + blank(item.ruleId())
        + " simbolo:" + blank(item.symbolName()) + " ubicacion:" + location
        + " severidad:" + blank(item.severityHint()) + " confianza:"
        + String.format("%.2f", item.confidence()) + "\n   texto: " + blank(item.matchText())
        + tags;
  }

  private static String context(CodeSignalEvidence item) {
    String matchPrefix = item.matchLine() + ":";
    List<String> lines = item.snippet() == null ? List.of() : item.snippet().stream()
        .filter(line -> !line.startsWith(matchPrefix))
        .map(AgentEvidenceFormatter::compactLine)
        .filter(StringUtils::isNotBlank)
        .toList();
    if (lines.isEmpty()) {
      return "";
    }
    return "\n   contexto:\n" + lines.stream().map(line -> CONTEXT_INDENT + line)
        .collect(Collectors.joining("\n"));
  }

  private static String compactLine(String snippetLine) {
    int separator = snippetLine.indexOf(':');
    String code = snippetLine.substring(separator + 1).strip();
    if (code.isEmpty()) {
      return "";
    }
    return StringUtils.abbreviate(snippetLine.substring(0, separator) + ": " + code,
        MAX_CONTEXT_LINE_CHARS);
  }

  private static String blank(String value) {
    return StringUtils.defaultIfBlank(value, "n/d");
  }
}
