package com.pichincha.cdemsaspopenaidocumentation.copilot.impl;

import com.pichincha.cdemsaspopenaidocumentation.copilot.PromptCommandExecutor;
import com.pichincha.cdemsaspopenaidocumentation.copilot.PromptRegistryService;
import com.pichincha.cdemsaspopenaidocumentation.copilot.config.DocumentationAgentsProperties;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentationAgentType;
import org.springframework.stereotype.Component;

@Component
public class ApiAgent extends AbstractSpecialistAgent {

  private final DocumentationAgentsProperties agentsProperties;

  public ApiAgent(PromptRegistryService promptRegistryService,
      PromptCommandExecutor promptCommandExecutor,
      DocumentationAgentsProperties agentsProperties) {
    super(promptRegistryService, promptCommandExecutor);
    this.agentsProperties = agentsProperties;
  }

  @Override
  public DocumentationAgentType agentType() {
    return DocumentationAgentType.API;
  }

  @Override
  String promptId() {
    return "api-agent";
  }

  @Override
  String model() {
    return agentsProperties.api().model();
  }

  @Override
  String objective() {
    return "endpoints expuestos, clientes de integracion y contratos de API";
  }
}
