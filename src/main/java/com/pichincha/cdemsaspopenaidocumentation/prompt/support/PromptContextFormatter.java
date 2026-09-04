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
    return String.join("\n\n",
        formatRepositoryMetadataSection(context.repositoryMetadata()),
        formatEvidencesSection(context.evidences()),
        formatCurrentDocumentationSection(context.currentDocumentation()),
        formatHistoricalIncidentsSection(context.historicalIncidents()));
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
    StringJoiner joiner = new StringJoiner("\n", index + ".\n", "");
    append(joiner, "signalType", evidence.signalType());
    append(joiner, "technology", evidence.technology());
    append(joiner, "filePath", evidence.filePath());
    append(joiner, "matchText", evidence.matchText());
    append(joiner, "confidence", Double.toString(evidence.confidence()));
    append(joiner, "language", evidence.language());
    append(joiner, "sourceKind", evidence.sourceKind());
    append(joiner, "ruleId", evidence.ruleId());
    append(joiner, "matchedRule", evidence.matchedRule());
    append(joiner, "probableCause", evidence.probableCause());
    append(joiner, "recoveryAction", evidence.recoveryAction());
    append(joiner, "tags", String.join(", ", evidence.tags()));
    return joiner.toString();
  }

  private String formatCurrentDocumentationSection(String currentDocumentation) {
    return "## Current Documentation\n"
        + StringUtils.defaultIfBlank(currentDocumentation, "No current documentation provided.");
  }

  private String formatHistoricalIncidentsSection(List<String> incidents) {
    StringJoiner joiner = new StringJoiner("\n", "## Historical Incidents\n", "");
    if (incidents.isEmpty()) {
      joiner.add("- No historical incidents provided.");
      return joiner.toString();
    }
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
}