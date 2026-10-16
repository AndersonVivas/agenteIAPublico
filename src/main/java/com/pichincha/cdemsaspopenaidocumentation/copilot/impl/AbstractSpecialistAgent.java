package com.pichincha.cdemsaspopenaidocumentation.copilot.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pichincha.cdemsaspopenaidocumentation.copilot.PromptCommandExecutor;
import com.pichincha.cdemsaspopenaidocumentation.copilot.PromptRegistryService;
import com.pichincha.cdemsaspopenaidocumentation.copilot.SpecialistDocumentationAgent;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.AgentFinding;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.AgentKnowledgeResult;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.CopilotExecutionOutput;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.PromptExecutionStatus;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.SpecialistAgentRequest;
import com.pichincha.cdemsaspopenaidocumentation.copilot.exception.AgentExecutionException;
import com.pichincha.cdemsaspopenaidocumentation.copilot.support.AgentEvidenceFormatter;
import com.pichincha.cdemsaspopenaidocumentation.copilot.support.EvidenceReferenceFilter;
import com.pichincha.cdemsaspopenaidocumentation.copilot.support.JsonResponseExtractor;
import com.pichincha.cdemsaspopenaidocumentation.copilot.support.PromptPlaceholderRenderer;
import com.pichincha.cdemsaspopenaidocumentation.copilot.support.SectionContextPromptFormatter;
import com.pichincha.cdemsaspopenaidocumentation.domain.CodeSignalEvidence;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Shared execution template for every specialist agent: load its external prompt, render
 * the partitioned evidence and relevant section contexts into it, call the Copilot CLI with
 * the agent's configured model, and parse the strict-JSON structured knowledge response.
 * A specialist agent never produces final documentation text.
 */
abstract class AbstractSpecialistAgent implements SpecialistDocumentationAgent {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private final PromptRegistryService promptRegistryService;
  private final PromptCommandExecutor promptCommandExecutor;

  AbstractSpecialistAgent(PromptRegistryService promptRegistryService,
      PromptCommandExecutor promptCommandExecutor) {
    this.promptRegistryService = promptRegistryService;
    this.promptCommandExecutor = promptCommandExecutor;
  }

  @Override
  public final AgentKnowledgeResult execute(SpecialistAgentRequest request) {
    String promptTemplate = promptRegistryService.loadPrompt(promptId());
    String prompt = PromptPlaceholderRenderer.render(promptTemplate, placeholders(request));
    String model = model();
    CopilotExecutionOutput output = promptCommandExecutor.execute(prompt, model);
    if (output.status() != PromptExecutionStatus.SUCCESS) {
      throw new AgentExecutionException(agentType(), "Copilot CLI did not complete "
          + "successfully: " + output.stderr());
    }
    return EvidenceReferenceFilter.retainKnownEvidence(parseResult(output.stdout(), model),
        knownEvidenceIds(request));
  }

  private Set<String> knownEvidenceIds(SpecialistAgentRequest request) {
    return request.evidence().stream().map(CodeSignalEvidence::evidenceId)
        .collect(Collectors.toSet());
  }

  private Map<String, String> placeholders(SpecialistAgentRequest request) {
    return Map.of(
        "repositoryName", request.generationContext().repository().repositoryName(),
        "organization", request.generationContext().repository().organization(),
        "projectName", request.generationContext().repository().projectName(),
        "objective", objective(),
        "evidence", AgentEvidenceFormatter.format(request.evidence()),
        "sections", SectionContextPromptFormatter.format(request.relevantSections()));
  }

  private AgentKnowledgeResult parseResult(String rawOutput, String model) {
    try {
      String json = JsonResponseExtractor.extractObject(rawOutput);
      SpecialistResponse response = MAPPER.readValue(json, SpecialistResponse.class);
      return new AgentKnowledgeResult(agentType(), response.findings(), response.gaps(),
          response.warnings(), model);
    } catch (RuntimeException | java.io.IOException exception) {
      throw new AgentExecutionException(agentType(),
          "Unable to parse structured JSON knowledge response.", exception);
    }
  }

  abstract String promptId();

  abstract String model();

  abstract String objective();

  @JsonIgnoreProperties(ignoreUnknown = true)
  private record SpecialistResponse(List<AgentFinding> findings, List<String> gaps,
      List<String> warnings) {

    private SpecialistResponse {
      findings = findings == null ? List.of() : findings;
      gaps = gaps == null ? List.of() : gaps;
      warnings = warnings == null ? List.of() : warnings;
    }
  }
}
