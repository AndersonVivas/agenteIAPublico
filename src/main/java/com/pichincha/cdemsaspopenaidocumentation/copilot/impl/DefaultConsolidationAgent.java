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
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentationAgentType;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.PromptExecutionStatus;
import com.pichincha.cdemsaspopenaidocumentation.copilot.exception.AgentExecutionException;
import com.pichincha.cdemsaspopenaidocumentation.copilot.support.JsonResponseExtractor;
import com.pichincha.cdemsaspopenaidocumentation.copilot.support.PromptPlaceholderRenderer;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * Merges the structured knowledge produced by every specialist agent into a single,
 * deduplicated and conflict-aware view. Still produces structured knowledge only; the
 * final Markdown is produced downstream by the Template Render Agent.
 */
@Service
public class DefaultConsolidationAgent implements ConsolidationAgent {

  private static final String PROMPT_ID = "consolidation-agent";
  private static final String AGENT_LABEL = "consolidation";
  private static final ObjectMapper MAPPER = new ObjectMapper();

  private final PromptRegistryService promptRegistryService;
  private final PromptCommandExecutor promptCommandExecutor;
  private final DocumentationAgentsProperties agentsProperties;

  public DefaultConsolidationAgent(PromptRegistryService promptRegistryService,
      PromptCommandExecutor promptCommandExecutor,
      DocumentationAgentsProperties agentsProperties) {
    this.promptRegistryService = promptRegistryService;
    this.promptCommandExecutor = promptCommandExecutor;
    this.agentsProperties = agentsProperties;
  }

  @Override
  public ConsolidatedKnowledge consolidate(ConsolidationRequest request) {
    String template = promptRegistryService.loadPrompt(PROMPT_ID);
    String prompt = PromptPlaceholderRenderer.render(template, placeholders(request));
    String model = agentsProperties.consolidation().model();
    CopilotExecutionOutput output = promptCommandExecutor.execute(prompt, model);
    if (output.status() != PromptExecutionStatus.SUCCESS) {
      throw new AgentExecutionException(AGENT_LABEL, "Consolidation Copilot CLI call failed: "
          + output.stderr());
    }
    return parseResult(output.stdout());
  }

  private Map<String, String> placeholders(ConsolidationRequest request) {
    return Map.of(
        "repositoryName", request.generationContext().repositoryName(),
        "organization", request.generationContext().organization(),
        "projectName", request.generationContext().projectName(),
        "specialistFindings", formatSpecialistResults(request.specialistResults()),
        "knowledgeGaps", formatList(request.knowledgeGaps()),
        "knowledgeConflicts", formatList(request.knowledgeConflicts()));
  }

  private String formatSpecialistResults(List<AgentKnowledgeResult> results) {
    if (results.isEmpty()) {
      return "- Ningun agente especialista produjo hallazgos.";
    }
    StringBuilder builder = new StringBuilder();
    for (AgentKnowledgeResult result : results) {
      builder.append("### ").append(result.agentType()).append('\n');
      if (result.findings().isEmpty()) {
        builder.append("- Sin hallazgos.\n");
      }
      for (AgentFinding finding : result.findings()) {
        builder.append("- [").append(result.agentType()).append("] ").append(finding.title())
            .append(" | negocio: ").append(finding.businessSummary())
            .append(" | tecnico: ").append(finding.technicalSummary())
            .append(" | severidad: ").append(finding.severity())
            .append(" | evidencia: ").append(String.join(",", finding.evidenceIds()))
            .append('\n');
      }
      if (!result.gaps().isEmpty()) {
        builder.append("- vacios: ").append(String.join(" ; ", result.gaps())).append('\n');
      }
    }
    return builder.toString().trim();
  }

  private String formatList(List<String> values) {
    if (values == null || values.isEmpty()) {
      return "- Ninguno.";
    }
    return values.stream().map(value -> "- " + value)
        .reduce((first, second) -> first + "\n" + second).orElse("- Ninguno.");
  }

  private ConsolidatedKnowledge parseResult(String rawOutput) {
    try {
      String json = JsonResponseExtractor.extractObject(rawOutput);
      ConsolidationResponse response = MAPPER.readValue(json, ConsolidationResponse.class);
      return toConsolidatedKnowledge(response);
    } catch (RuntimeException | java.io.IOException exception) {
      throw new AgentExecutionException(AGENT_LABEL,
          "Unable to parse consolidated JSON knowledge response.", exception);
    }
  }

  private ConsolidatedKnowledge toConsolidatedKnowledge(ConsolidationResponse response) {
    Map<DocumentationAgentType, List<AgentFinding>> byAgent =
        new EnumMap<>(DocumentationAgentType.class);
    List<AgentFinding> flat = new ArrayList<>();
    for (ConsolidatedFinding item : response.findings()) {
      AgentFinding finding = new AgentFinding(item.findingId(), item.title(),
          item.businessSummary(), item.technicalSummary(), item.severity(),
          item.evidenceIds());
      flat.add(finding);
      resolveCategory(item.category()).ifPresent(type -> byAgent
          .computeIfAbsent(type, ignored -> new ArrayList<>()).add(finding));
    }
    return new ConsolidatedKnowledge(flat, byAgent, response.unresolvedConflicts(),
        response.warnings());
  }

  private java.util.Optional<DocumentationAgentType> resolveCategory(String category) {
    try {
      String normalized = StringUtils.defaultString(category).trim()
          .toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
      return java.util.Optional.of(DocumentationAgentType.valueOf(normalized));
    } catch (IllegalArgumentException exception) {
      return java.util.Optional.empty();
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
  private record ConsolidatedFinding(String category, String findingId, String title,
      String businessSummary, String technicalSummary, String severity,
      List<String> evidenceIds) {

    private ConsolidatedFinding {
      evidenceIds = evidenceIds == null ? List.of() : evidenceIds;
    }
  }
}
