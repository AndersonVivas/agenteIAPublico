package com.pichincha.cdemsaspopenaidocumentation.copilot.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Per-agent model configuration, one entry per specialist, consolidation, renderer and
 * editor agent. Every agent calls the Copilot CLI with its own model, enabling cheap models for
 * narrow extraction tasks and a stronger model only where synthesis quality matters.
 */
@ConfigurationProperties(prefix = "documentation.agents")
public record DocumentationAgentsProperties(
    AgentModel api,
    AgentModel dependencies,
    AgentModel configuration,
    AgentModel monitoring,
    AgentModel exceptions,
    AgentModel business,
    AgentModel consolidation,
    AgentModel renderer,
    AgentModel editor) {

  private static final String DEFAULT_SPECIALIST_MODEL = "gpt-5-mini";
  private static final String DEFAULT_SYNTHESIS_MODEL = "gpt-5";

  public DocumentationAgentsProperties {
    api = defaulted(api, DEFAULT_SPECIALIST_MODEL);
    dependencies = defaulted(dependencies, DEFAULT_SPECIALIST_MODEL);
    configuration = defaulted(configuration, DEFAULT_SPECIALIST_MODEL);
    monitoring = defaulted(monitoring, DEFAULT_SPECIALIST_MODEL);
    exceptions = defaulted(exceptions, DEFAULT_SPECIALIST_MODEL);
    business = defaulted(business, DEFAULT_SPECIALIST_MODEL);
    consolidation = defaulted(consolidation, DEFAULT_SYNTHESIS_MODEL);
    renderer = defaulted(renderer, DEFAULT_SYNTHESIS_MODEL);
    editor = defaulted(editor, DEFAULT_SYNTHESIS_MODEL);
  }

  private static AgentModel defaulted(AgentModel configured, String defaultModel) {
    if (configured == null || StringUtils.isBlank(configured.model())) {
      return new AgentModel(defaultModel);
    }
    return configured;
  }

  public record AgentModel(String model) {

    public AgentModel {
      model = StringUtils.defaultString(model).trim();
    }
  }
}
