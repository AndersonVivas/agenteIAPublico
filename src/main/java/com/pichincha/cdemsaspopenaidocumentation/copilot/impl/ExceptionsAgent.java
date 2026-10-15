package com.pichincha.cdemsaspopenaidocumentation.copilot.impl;

import com.pichincha.cdemsaspopenaidocumentation.copilot.PromptCommandExecutor;
import com.pichincha.cdemsaspopenaidocumentation.copilot.PromptRegistryService;
import com.pichincha.cdemsaspopenaidocumentation.copilot.config.DocumentationAgentsProperties;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentationAgentType;
import org.springframework.stereotype.Component;

@Component
public class ExceptionsAgent extends AbstractSpecialistAgent {

  private final DocumentationAgentsProperties agentsProperties;

  public ExceptionsAgent(PromptRegistryService promptRegistryService,
      PromptCommandExecutor promptCommandExecutor,
      DocumentationAgentsProperties agentsProperties) {
    super(promptRegistryService, promptCommandExecutor);
    this.agentsProperties = agentsProperties;
  }

  @Override
  public DocumentationAgentType agentType() {
    return DocumentationAgentType.EXCEPTION;
  }

  @Override
  String promptId() {
    return "exceptions-agent";
  }

  @Override
  String model() {
    return agentsProperties.exceptions().model();
  }

  @Override
  String objective() {
    return "manejo de excepciones, catalogo de errores y acciones de recuperacion";
  }
}
