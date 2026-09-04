package com.pichincha.cdemsaspopenaidocumentation.prompt.support;

import com.pichincha.cdemsaspopenaidocumentation.domain.CodeSignalEvidence;
import com.pichincha.cdemsaspopenaidocumentation.prompt.domain.PromptContext;
import com.pichincha.cdemsaspopenaidocumentation.prompt.domain.RepositoryMetadata;
import java.util.List;
import java.util.StringJoiner;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class PromptContextFormatter {

  public String formatEvidenceSection(PromptContext context) {
    StringJoiner joiner = new StringJoiner("\n\n");
    joiner.add(formatRepositoryMetadataSection(context.repositoryMetadata()));
    joiner.add(formatEvidencesSection(context.evidences()));
    String currentDoc = context.currentDocumentation();
    if (StringUtils.isNotBlank(currentDoc)) {
      joiner.add("## Current Documentation\n" + currentDoc.trim());
    }
    List<String> incidents = context.historicalIncidents();
    if (!incidents.isEmpty()) {
      joiner.add(formatHistoricalIncidentsSection(incidents));
    }
    return joiner.toString();
  }

  public String formatRagContext(String ragContext) {
    return StringUtils.isBlank(ragContext) ? "No RAG context provided." : ragContext.trim();
  }

  private String formatRepositoryMetadataSection(RepositoryMetadata metadata) {
    StringJoiner joiner = new StringJoiner("\n", "## Repository Metadata\n", "");
    append(joiner, "repositoryName", metadata.repositoryName());
    append(joiner, "repositoryPath", metadata.repositoryPath());
    append(joiner, "architecture", metadata.architecture());
    append(joiner, "primaryTechnology", metadata.primaryTechnology());
    metadata.attributes().forEach((key, value) -> append(joiner, key, value));
    if (joiner.length() == "## Repository Metadata\n".length()) {
      joiner.add("- No repository metadata provided.");
    }
    return joiner.toString();
  }

  private String formatEvidencesSection(List<CodeSignalEvidence> evidences) {
    StringJoiner joiner = new StringJoiner("\n", "## Evidences\n", "");
    if (evidences.isEmpty()) {
      joiner.add("- No evidences provided.");
      return joiner.toString();
    }
    for (int index = 0; index < evidences.size(); index++) {
      joiner.add(formatEvidence(index + 1, evidences.get(index)));
    }
    return joiner.toString();
  }

  private String formatEvidence(int index, CodeSignalEvidence evidence) {
    String location = buildLocation(evidence);
    String header = index + ". [" + blankSafe(evidence.signalType()) + "|"
        + blankSafe(evidence.technology()) + "|" + blankSafe(evidence.language()) + "] "
        + blankSafe(evidence.matchText()) + " → " + location
        + " | rule:" + blankSafe(evidence.ruleId())
        + " | conf:" + String.format("%.2f", evidence.confidence());
    String tags = evidence.tags() == null || evidence.tags().isEmpty()
        ? "" : " | tags:" + String.join(",", evidence.tags());
    String detail = "   cause:" + blankSafe(evidence.probableCause())
        + " | action:" + blankSafe(evidence.recoveryAction()) + tags;
    return header + "\n" + detail;
  }

  private String buildLocation(CodeSignalEvidence evidence) {
    String path = blankSafe(evidence.filePath());
    return evidence.matchLine() > 0 ? path + ":" + evidence.matchLine() : path;
  }

  private String formatHistoricalIncidentsSection(List<String> incidents) {
    StringJoiner joiner = new StringJoiner("\n", "## Historical Incidents\n", "");
    for (int index = 0; index < incidents.size(); index++) {
      joiner.add("- " + (index + 1) + ". " + StringUtils.defaultString(incidents.get(index)));
    }
    return joiner.toString();
  }

  private void append(StringJoiner joiner, String label, String value) {
    if (StringUtils.isBlank(value)) {
      return;
    }
    joiner.add("- " + label + ": " + value.trim());
  }

  private String blankSafe(String value) {
    return StringUtils.defaultIfBlank(value, "");
  }
}