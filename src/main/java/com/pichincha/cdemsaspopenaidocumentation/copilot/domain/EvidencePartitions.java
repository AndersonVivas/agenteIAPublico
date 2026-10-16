package com.pichincha.cdemsaspopenaidocumentation.copilot.domain;

import com.pichincha.cdemsaspopenaidocumentation.domain.CodeSignalEvidence;
import java.util.List;

public record EvidencePartitions(
    ApiEvidenceSet api,
    ExceptionEvidenceSet exceptions,
    DependencyEvidenceSet dependencies,
    ConfigurationEvidenceSet configuration,
    MonitoringEvidenceSet monitoring,
    BusinessFlowEvidenceSet businessFlow) {

  public List<CodeSignalEvidence> evidenceFor(DocumentationAgentType agentType) {
    return switch (agentType) {
      case API -> api.evidences();
      case EXCEPTION -> exceptions.evidences();
      case DEPENDENCY -> dependencies.evidences();
      case CONFIGURATION -> configuration.evidences();
      case MONITORING -> monitoring.evidences();
      case BUSINESS_FLOW -> businessFlow.evidences();
    };
  }
}
