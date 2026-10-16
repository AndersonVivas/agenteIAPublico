package com.pichincha.cdemsaspopenaidocumentation.copilot.impl;

import com.pichincha.cdemsaspopenaidocumentation.copilot.EditorialReviewAgent;
import com.pichincha.cdemsaspopenaidocumentation.copilot.PromptCommandExecutor;
import com.pichincha.cdemsaspopenaidocumentation.copilot.PromptRegistryService;
import com.pichincha.cdemsaspopenaidocumentation.copilot.config.DocumentationAgentsProperties;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.CopilotExecutionOutput;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentContext;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.EditorialReviewRequest;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.PromptExecutionStatus;
import com.pichincha.cdemsaspopenaidocumentation.copilot.exception.AgentExecutionException;
import com.pichincha.cdemsaspopenaidocumentation.copilot.support.GlobalRulesPromptFormatter;
import com.pichincha.cdemsaspopenaidocumentation.copilot.support.PromptPlaceholderRenderer;
import com.pichincha.cdemsaspopenaidocumentation.copilot.support.ReaderProfilePromptFormatter;
import com.pichincha.cdemsaspopenaidocumentation.copilot.support.SectionBodyResponseParser;
import com.pichincha.cdemsaspopenaidocumentation.copilot.support.SkillPromptFormatter;
import com.pichincha.cdemsaspopenaidocumentation.util.HashUtil;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DefaultEditorialReviewAgent implements EditorialReviewAgent {

  private static final String PROMPT_ID = "editorial-review-agent";
  private static final String AGENT_LABEL = "editorial-review";

  private final PromptRegistryService promptRegistryService;
  private final PromptCommandExecutor promptCommandExecutor;
  private final DocumentationAgentsProperties agentsProperties;

  @Override
  public Map<String, String> review(EditorialReviewRequest request) {
    String prompt = PromptPlaceholderRenderer.render(
        promptRegistryService.loadPrompt(PROMPT_ID), placeholders(request));
    CopilotExecutionOutput output = promptCommandExecutor.execute(prompt,
        agentsProperties.editor().model());
    if (output.status() != PromptExecutionStatus.SUCCESS) {
      throw new AgentExecutionException(AGENT_LABEL, "Editorial review Copilot CLI call "
          + "failed: " + output.stderr());
    }
    return SectionBodyResponseParser.parse(output.stdout(), request.drafts().keySet(),
        AGENT_LABEL);
  }

  @Override
  public String reviewSignature() {
    return HashUtil.sha256Hex(promptRegistryService.loadPrompt(PROMPT_ID) + "|"
        + agentsProperties.editor().model());
  }

  private Map<String, String> placeholders(EditorialReviewRequest request) {
    DocumentContext context = request.documentContext();
    return Map.of(
        "templateName", context.template().name(),
        "outputLanguage", context.outputContract().outputLanguage(),
        "missingDataMessage", context.outputContract().missingDataMessage(),
        "readerProfile", ReaderProfilePromptFormatter.format(context.template().reader()),
        "skills", SkillPromptFormatter.format(context.skills()),
        "globalRules", GlobalRulesPromptFormatter.format(context.outputContract().rules()),
        "drafts", formatDrafts(request));
  }

  private String formatDrafts(EditorialReviewRequest request) {
    return request.sections().stream()
        .filter(section -> request.drafts().containsKey(section.sectionId()))
        .map(section -> "<seccion id=\"" + section.sectionId() + "\" titulo=\""
            + section.title() + "\">\n" + request.drafts().get(section.sectionId())
            + "\n</seccion>")
        .collect(Collectors.joining("\n\n"));
  }
}
