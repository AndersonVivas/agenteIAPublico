package com.pichincha.cdemsaspopenaidocumentation.copilot.impl;

import com.pichincha.cdemsaspopenaidocumentation.copilot.PromptCommandExecutor;
import com.pichincha.cdemsaspopenaidocumentation.copilot.PromptRegistryService;
import com.pichincha.cdemsaspopenaidocumentation.copilot.TemplateRenderAgent;
import com.pichincha.cdemsaspopenaidocumentation.copilot.config.DocumentationAgentsProperties;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.AgentFinding;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.ConsolidatedKnowledge;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.CopilotExecutionOutput;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentContext;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.OutputContract;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.PromptExecutionStatus;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.TemplateRenderRequest;
import com.pichincha.cdemsaspopenaidocumentation.copilot.exception.AgentExecutionException;
import com.pichincha.cdemsaspopenaidocumentation.copilot.support.ChangeContextFormatter;
import com.pichincha.cdemsaspopenaidocumentation.copilot.support.GlobalRulesPromptFormatter;
import com.pichincha.cdemsaspopenaidocumentation.copilot.support.PromptPlaceholderRenderer;
import com.pichincha.cdemsaspopenaidocumentation.copilot.support.ReaderProfilePromptFormatter;
import com.pichincha.cdemsaspopenaidocumentation.copilot.support.SectionBodyResponseParser;
import com.pichincha.cdemsaspopenaidocumentation.copilot.support.SectionContextPromptFormatter;
import com.pichincha.cdemsaspopenaidocumentation.copilot.support.SkillPromptFormatter;
import com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.domain.SectionContext;
import com.pichincha.cdemsaspopenaidocumentation.domain.RepositoryCoordinates;
import com.pichincha.cdemsaspopenaidocumentation.util.HashUtil;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Produces the final Markdown body for the impacted sections only, driven entirely by the
 * rendering plan's {@code SectionContext} list (title, objective, instructions, presentation
 * mode, highlights and traceability warnings), the reader profile, the writing skills and
 * global rules of the template, the change context of the scan and the consolidated structured
 * knowledge. This is the only agent in the pipeline allowed to emit Markdown.
 */
@Service
@RequiredArgsConstructor
public class DefaultTemplateRenderAgent implements TemplateRenderAgent {

  private static final String PROMPT_ID = "template-render-agent";
  private static final String AGENT_LABEL = "template-render";
  private static final String NO_FINDINGS = "- No hay hallazgos consolidados disponibles.";

  private final PromptRegistryService promptRegistryService;
  private final PromptCommandExecutor promptCommandExecutor;
  private final DocumentationAgentsProperties agentsProperties;

  @Override
  public Map<String, String> render(TemplateRenderRequest request) {
    if (request.impactedSections().isEmpty()) {
      return Map.of();
    }
    String template = promptRegistryService.loadPrompt(PROMPT_ID);
    String prompt = PromptPlaceholderRenderer.render(template, placeholders(request));
    CopilotExecutionOutput output = promptCommandExecutor.execute(prompt,
        agentsProperties.renderer().model());
    if (output.status() != PromptExecutionStatus.SUCCESS) {
      throw new AgentExecutionException(AGENT_LABEL, "Template Render Copilot CLI call "
          + "failed: " + output.stderr());
    }
    return SectionBodyResponseParser.parse(output.stdout(), request.impactedSections().stream()
        .map(SectionContext::sectionId).collect(Collectors.toSet()), AGENT_LABEL);
  }

  @Override
  public String renderSignature() {
    return HashUtil.sha256Hex(promptRegistryService.loadPrompt(PROMPT_ID) + "|"
        + agentsProperties.renderer().model());
  }

  private Map<String, String> placeholders(TemplateRenderRequest request) {
    DocumentContext context = request.documentContext();
    RepositoryCoordinates repository = context.generationContext().repository();
    OutputContract contract = context.outputContract();
    return Map.ofEntries(
        Map.entry("repositoryName", repository.repositoryName()),
        Map.entry("organization", repository.organization()),
        Map.entry("projectName", repository.projectName()),
        Map.entry("templateName", context.template().name()),
        Map.entry("documentLifecycle", context.lifecycle().name()),
        Map.entry("outputLanguage", contract.outputLanguage()),
        Map.entry("missingDataMessage", contract.missingDataMessage()),
        Map.entry("globalRules", GlobalRulesPromptFormatter.format(contract.rules())),
        Map.entry("readerProfile",
            ReaderProfilePromptFormatter.format(context.template().reader())),
        Map.entry("skills", SkillPromptFormatter.format(context.skills())),
        Map.entry("changeContext", ChangeContextFormatter.format(context.generationContext())),
        Map.entry("sections", SectionContextPromptFormatter.format(request.impactedSections())),
        Map.entry("consolidatedFindings", formatFindings(request.consolidatedKnowledge())));
  }

  private String formatFindings(ConsolidatedKnowledge knowledge) {
    if (knowledge.findings().isEmpty()) {
      return NO_FINDINGS;
    }
    return knowledge.findings().stream().map(this::formatFinding)
        .collect(Collectors.joining("\n"));
  }

  private String formatFinding(AgentFinding finding) {
    return "- [" + finding.findingId() + "] " + finding.title()
        + " | negocio: " + finding.businessSummary()
        + " | tecnico: " + finding.technicalSummary()
        + " | severidad: " + finding.severity()
        + " | evidencia: " + String.join(",", finding.evidenceIds());
  }
}
