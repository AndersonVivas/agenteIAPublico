package com.pichincha.cdemsaspopenaidocumentation.copilot.impl;

import com.pichincha.cdemsaspopenaidocumentation.copilot.PromptCommandExecutor;
import com.pichincha.cdemsaspopenaidocumentation.copilot.PromptRegistryService;
import com.pichincha.cdemsaspopenaidocumentation.copilot.config.DocumentationAgentsProperties;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentationAgentType;
import org.springframework.stereotype.Component;

@Component
public class BusinessFlowAgent extends AbstractSpecialistAgent {

  private final DocumentationAgentsProperties agentsProperties;

  public BusinessFlowAgent(PromptRegistryService promptRegistryService,
      PromptCommandExecutor promptCommandExecutor,
      DocumentationAgentsProperties agentsProperties) {
    super(promptRegistryService, promptCommandExecutor);
    this.agentsProperties = agentsProperties;
  }

  @Override
  public DocumentationAgentType agentType() {
    return DocumentationAgentType.BUSINESS_FLOW;
  }

  @Override
  String promptId() {
    return "business-flow-agent";
  }

  @Override
  String model() {
    return agentsProperties.business().model();
  }

  @Override
  String objective() {
    return "procesos de negocio, reglas y su impacto funcional observable";
  }
}
