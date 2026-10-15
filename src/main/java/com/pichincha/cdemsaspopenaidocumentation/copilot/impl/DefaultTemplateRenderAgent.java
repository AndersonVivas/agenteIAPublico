package com.pichincha.cdemsaspopenaidocumentation.copilot.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pichincha.cdemsaspopenaidocumentation.copilot.PromptCommandExecutor;
import com.pichincha.cdemsaspopenaidocumentation.copilot.PromptRegistryService;
import com.pichincha.cdemsaspopenaidocumentation.copilot.TemplateRenderAgent;
import com.pichincha.cdemsaspopenaidocumentation.copilot.config.DocumentationAgentsProperties;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.AgentFinding;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.ConsolidatedKnowledge;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.CopilotExecutionOutput;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.OutputContract;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.PromptExecutionStatus;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.TemplateRenderRequest;
import com.pichincha.cdemsaspopenaidocumentation.copilot.exception.AgentExecutionException;
import com.pichincha.cdemsaspopenaidocumentation.copilot.support.JsonResponseExtractor;
import com.pichincha.cdemsaspopenaidocumentation.copilot.support.PromptPlaceholderRenderer;
import com.pichincha.cdemsaspopenaidocumentation.copilot.support.SectionContextPromptFormatter;
import com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.domain.SectionContext;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * Produces the final Markdown body for the impacted sections only, driven entirely by the
 * rendering plan's {@code SectionContext} list (title, objective, narrative intent,
 * presentation mode, highlights and traceability warnings) plus the consolidated structured
 * knowledge. This is the only agent in the pipeline allowed to emit Markdown.
 */
@Service
public class DefaultTemplateRenderAgent implements TemplateRenderAgent {

  private static final String PROMPT_ID = "template-render-agent";
  private static final String AGENT_LABEL = "template-render";
  private static final ObjectMapper MAPPER = new ObjectMapper();

  private final PromptRegistryService promptRegistryService;
  private final PromptCommandExecutor promptCommandExecutor;
  private final DocumentationAgentsProperties agentsProperties;

  public DefaultTemplateRenderAgent(PromptRegistryService promptRegistryService,
      PromptCommandExecutor promptCommandExecutor,
      DocumentationAgentsProperties agentsProperties) {
    this.promptRegistryService = promptRegistryService;
    this.promptCommandExecutor = promptCommandExecutor;
    this.agentsProperties = agentsProperties;
  }

  @Override
  public Map<String, String> render(TemplateRenderRequest request) {
    if (request.impactedSections().isEmpty()) {
      return Map.of();
    }
    String template = promptRegistryService.loadPrompt(PROMPT_ID);
    String prompt = PromptPlaceholderRenderer.render(template, placeholders(request));
    String model = agentsProperties.renderer().model();
    CopilotExecutionOutput output = promptCommandExecutor.execute(prompt, model);
    if (output.status() != PromptExecutionStatus.SUCCESS) {
      throw new AgentExecutionException(AGENT_LABEL, "Template Render Copilot CLI call "
          + "failed: " + output.stderr());
    }
    return parseResult(output.stdout(), request.impactedSections());
  }

  private Map<String, String> placeholders(TemplateRenderRequest request) {
    OutputContract contract = request.outputContract();
    return Map.of(
        "repositoryName", request.generationContext().repositoryName(),
        "organization", request.generationContext().organization(),
        "projectName", request.generationContext().projectName(),
        "templateName", request.resolvedTemplateContext().template().name(),
        "outputLanguage", contract.outputLanguage(),
        "missingDataMessage", contract.missingDataMessage(),
        "sections", SectionContextPromptFormatter.format(request.impactedSections()),
        "consolidatedFindings", formatFindings(request.consolidatedKnowledge()));
  }

  private String formatFindings(ConsolidatedKnowledge knowledge) {
    List<AgentFinding> findings = knowledge.findings();
    if (findings.isEmpty()) {
      return "- No hay hallazgos consolidados disponibles.";
    }
    StringBuilder builder = new StringBuilder();
    for (AgentFinding finding : findings) {
      builder.append("- [").append(finding.findingId()).append("] ").append(finding.title())
          .append(" | negocio: ").append(finding.businessSummary())
          .append(" | tecnico: ").append(finding.technicalSummary())
          .append(" | severidad: ").append(finding.severity())
          .append(" | evidencia: ").append(String.join(",", finding.evidenceIds()))
          .append('\n');
    }
    return builder.toString().trim();
  }

  private Map<String, String> parseResult(String rawOutput, List<SectionContext> impacted) {
    try {
      String json = JsonResponseExtractor.extractObject(rawOutput);
      Map<String, String> raw = MAPPER.readValue(json, new TypeReference<>() { });
      return filterToImpacted(raw, impacted);
    } catch (RuntimeException | java.io.IOException exception) {
      throw new AgentExecutionException(AGENT_LABEL,
          "Unable to parse rendered Markdown JSON response.", exception);
    }
  }

  private Map<String, String> filterToImpacted(Map<String, String> raw,
      List<SectionContext> impacted) {
    Set<String> impactedIds = impacted.stream().map(SectionContext::sectionId)
        .collect(java.util.stream.Collectors.toSet());
    Map<String, String> result = new LinkedHashMap<>();
    raw.forEach((sectionId, markdown) -> {
      if (impactedIds.contains(sectionId) && StringUtils.isNotBlank(markdown)) {
        result.put(sectionId, markdown.trim());
      }
    });
    return Map.copyOf(result);
  }
}
