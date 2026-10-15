package com.pichincha.cdemsaspopenaidocumentation.copilot.support;

import com.pichincha.cdemsaspopenaidocumentation.domain.CodeSignalEvidence;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 * Renders a compact, token-efficient textual view of a partitioned evidence slice for
 * inclusion in a specialist agent prompt. Only the fields an agent needs to reason about
 * and cite back in {@code AgentFinding.evidenceIds} are included.
 */
public final class AgentEvidenceFormatter {

  private AgentEvidenceFormatter() {
  }

  public static String format(List<CodeSignalEvidence> evidence) {
    if (evidence == null || evidence.isEmpty()) {
      return "- No se encontro evidencia para esta categoria en el escaneo estatico.";
    }
    StringBuilder builder = new StringBuilder();
    for (int index = 0; index < evidence.size(); index++) {
      builder.append(formatOne(index + 1, evidence.get(index))).append('\n');
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

  private static String blank(String value) {
    return StringUtils.defaultIfBlank(value, "n/d");
  }
}
