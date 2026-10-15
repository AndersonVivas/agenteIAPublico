package com.pichincha.cdemsaspopenaidocumentation.copilot;

import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.AgentKnowledgeResult;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentationAgentType;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.SpecialistAgentRequest;

/**
 * A specialist agent turns a narrow, pre-partitioned slice of evidence into structured
 * knowledge. It never produces final documentation text. The method signature is plain
 * (serializable request/response, no Spring or Temporal types) so it can later be invoked
 * from inside a Temporal Activity without changing this contract.
 */
public interface SpecialistDocumentationAgent {

  DocumentationAgentType agentType();

  AgentKnowledgeResult execute(SpecialistAgentRequest request);
}
