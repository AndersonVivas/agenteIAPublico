package com.pichincha.cdemsaspopenaidocumentation.copilot.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pichincha.cdemsaspopenaidocumentation.copilot.ConsolidationAgent;
import com.pichincha.cdemsaspopenaidocumentation.copilot.PromptCommandExecutor;
import com.pichincha.cdemsaspopenaidocumentation.copilot.PromptRegistryService;
import com.pichincha.cdemsaspopenaidocumentation.copilot.config.DocumentationAgentsProperties;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.AgentFinding;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.AgentKnowledgeResult;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.ConsolidatedKnowledge;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.ConsolidationRequest;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.CopilotExecutionOutput;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.PromptExecutionStatus;
import com.pichincha.cdemsaspopenaidocumentation.copilot.exception.AgentExecutionException;
import com.pichincha.cdemsaspopenaidocumentation.copilot.support.JsonResponseExtractor;
import com.pichincha.cdemsaspopenaidocumentation.copilot.support.PromptPlaceholderRenderer;
import com.pichincha.cdemsaspopenaidocumentation.domain.RepositoryCoordinates;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Merges the structured knowledge produced by every specialist agent into a single,
 * deduplicated and conflict-aware view. Still produces structured knowledge only; the
 * final Markdown is produced downstream by the Template Render Agent.
 */
@Service
@RequiredArgsConstructor
public class DefaultConsolidationAgent implements ConsolidationAgent {

  private static final String PROMPT_ID = "consolidation-agent";
  private static final String AGENT_LABEL = "consolidation";
  private static final String NO_FINDINGS = "- Ningun agente especialista produjo hallazgos.";
  private static final String NONE = "- Ninguno.";
  private static final ObjectMapper MAPPER = new ObjectMapper();

  private final PromptRegistryService promptRegistryService;
  private final PromptCommandExecutor promptCommandExecutor;
  private final DocumentationAgentsProperties agentsProperties;

  @Override
  public ConsolidatedKnowledge consolidate(ConsolidationRequest request) {
    String template = promptRegistryService.loadPrompt(PROMPT_ID);
    String prompt = PromptPlaceholderRenderer.render(template, placeholders(request));
    CopilotExecutionOutput output = promptCommandExecutor.execute(prompt,
        agentsProperties.consolidation().model());
    if (output.status() != PromptExecutionStatus.SUCCESS) {
      throw new AgentExecutionException(AGENT_LABEL, "Consolidation Copilot CLI call failed: "
          + output.stderr());
    }
    return parseResult(output.stdout());
  }

  private Map<String, String> placeholders(ConsolidationRequest request) {
    RepositoryCoordinates repository = request.generationContext().repository();
    return Map.of(
        "repositoryName", repository.repositoryName(),
        "organization", repository.organization(),
        "projectName", repository.projectName(),
        "specialistFindings", formatSpecialistResults(request.specialistResults()),
        "knowledgeGaps", formatList(request.knowledgeGaps()),
        "knowledgeConflicts", formatList(request.knowledgeConflicts()));
  }

  private String formatSpecialistResults(List<AgentKnowledgeResult> results) {
    if (results.isEmpty()) {
      return NO_FINDINGS;
    }
    return results.stream().map(this::formatResult).collect(Collectors.joining("\n")).trim();
  }

  private String formatResult(AgentKnowledgeResult result) {
    StringBuilder builder = new StringBuilder("### ").append(result.agentType()).append('\n');
    if (result.findings().isEmpty()) {
      builder.append("- Sin hallazgos.\n");
    }
    result.findings().forEach(finding -> builder.append(formatFinding(result, finding)));
    if (!result.gaps().isEmpty()) {
      builder.append("- vacios: ").append(String.join(" ; ", result.gaps())).append('\n');
    }
    return builder.toString();
  }

  private String formatFinding(AgentKnowledgeResult result, AgentFinding finding) {
    return "- [" + result.agentType() + "] " + finding.title()
        + " | negocio: " + finding.businessSummary()
        + " | tecnico: " + finding.technicalSummary()
        + " | severidad: " + finding.severity()
        + " | evidencia: " + String.join(",", finding.evidenceIds()) + "\n";
  }

  private String formatList(List<String> values) {
    if (values == null || values.isEmpty()) {
      return NONE;
    }
    return values.stream().map(value -> "- " + value).collect(Collectors.joining("\n"));
  }

  private ConsolidatedKnowledge parseResult(String rawOutput) {
    try {
      String json = JsonResponseExtractor.extractObject(rawOutput);
      ConsolidationResponse response = MAPPER.readValue(json, ConsolidationResponse.class);
      return new ConsolidatedKnowledge(response.findings().stream()
          .map(ConsolidatedFinding::toFinding).toList(), response.unresolvedConflicts(),
          response.warnings());
    } catch (RuntimeException | IOException exception) {
      throw new AgentExecutionException(AGENT_LABEL,
          "Unable to parse consolidated JSON knowledge response.", exception);
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  private record ConsolidationResponse(List<ConsolidatedFinding> findings,
      List<String> unresolvedConflicts, List<String> warnings) {

    private ConsolidationResponse {
      findings = findings == null ? List.of() : findings;
      unresolvedConflicts = unresolvedConflicts == null ? List.of() : unresolvedConflicts;
      warnings = warnings == null ? List.of() : warnings;
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  private record ConsolidatedFinding(String findingId, String title, String businessSummary,
      String technicalSummary, String severity, List<String> evidenceIds) {

    private AgentFinding toFinding() {
      return new AgentFinding(findingId, title, businessSummary, technicalSummary, severity,
          evidenceIds);
    }
  }
}
