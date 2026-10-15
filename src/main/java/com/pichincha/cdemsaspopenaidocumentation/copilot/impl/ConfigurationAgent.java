package com.pichincha.cdemsaspopenaidocumentation.copilot.impl;

import com.pichincha.cdemsaspopenaidocumentation.copilot.PromptCommandExecutor;
import com.pichincha.cdemsaspopenaidocumentation.copilot.PromptRegistryService;
import com.pichincha.cdemsaspopenaidocumentation.copilot.config.DocumentationAgentsProperties;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentationAgentType;
import org.springframework.stereotype.Component;

@Component
public class ConfigurationAgent extends AbstractSpecialistAgent {

  private final DocumentationAgentsProperties agentsProperties;

  public ConfigurationAgent(PromptRegistryService promptRegistryService,
      PromptCommandExecutor promptCommandExecutor,
      DocumentationAgentsProperties agentsProperties) {
    super(promptRegistryService, promptCommandExecutor);
    this.agentsProperties = agentsProperties;
  }

  @Override
  public DocumentationAgentType agentType() {
    return DocumentationAgentType.CONFIGURATION;
  }

  @Override
  String promptId() {
    return "configuration-agent";
  }

  @Override
  String model() {
    return agentsProperties.configuration().model();
  }

  @Override
  String objective() {
    return "configuracion externalizada, timeouts y riesgos operativos";
  }
}
