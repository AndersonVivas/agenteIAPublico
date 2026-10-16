package com.pichincha.cdemsaspopenaidocumentation.copilot.impl;

import com.pichincha.cdemsaspopenaidocumentation.copilot.EvidencePartitionService;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.ApiEvidenceSet;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.BusinessFlowEvidenceSet;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.ConfigurationEvidenceSet;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DependencyEvidenceSet;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentationAgentType;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.EvidencePartitions;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.ExceptionEvidenceSet;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.MonitoringEvidenceSet;
import com.pichincha.cdemsaspopenaidocumentation.domain.CodeSignalEvidence;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class DefaultEvidencePartitionService implements EvidencePartitionService {

  @Override
  public EvidencePartitions partition(List<CodeSignalEvidence> evidences) {
    Map<DocumentationAgentType, List<CodeSignalEvidence>> byAgent = evidences.stream()
        .sorted(Comparator.comparing(CodeSignalEvidence::evidenceId))
        .collect(Collectors.groupingBy(this::classify,
            () -> new EnumMap<>(DocumentationAgentType.class), Collectors.toList()));
    return new EvidencePartitions(
        new ApiEvidenceSet(byAgent.get(DocumentationAgentType.API)),
        new ExceptionEvidenceSet(byAgent.get(DocumentationAgentType.EXCEPTION)),
        new DependencyEvidenceSet(byAgent.get(DocumentationAgentType.DEPENDENCY)),
        new ConfigurationEvidenceSet(byAgent.get(DocumentationAgentType.CONFIGURATION)),
        new MonitoringEvidenceSet(byAgent.get(DocumentationAgentType.MONITORING)),
        new BusinessFlowEvidenceSet(byAgent.get(DocumentationAgentType.BUSINESS_FLOW)));
  }

  private DocumentationAgentType classify(CodeSignalEvidence evidence) {
    String signalType = normalized(evidence.signalType());
    if (signalType.equals("api")) {
      return isBusinessFlowEvidence(evidence)
          ? DocumentationAgentType.BUSINESS_FLOW : DocumentationAgentType.API;
    }
    if (signalType.equals("business_context")) {
      return DocumentationAgentType.BUSINESS_FLOW;
    }
    if (signalType.equals("exception")) {
      return DocumentationAgentType.EXCEPTION;
    }
    if (signalType.equals("dependency") || signalType.equals("database")) {
      return DocumentationAgentType.DEPENDENCY;
    }
    return isMonitoringEvidence(evidence)
        ? DocumentationAgentType.MONITORING : DocumentationAgentType.CONFIGURATION;
  }

  private boolean isBusinessFlowEvidence(CodeSignalEvidence evidence) {
    String searchable = String.join(" ", normalized(evidence.symbolName()),
        normalized(evidence.scopeSymbol()), normalized(evidence.filePath()),
        normalized(evidence.tags()));
    return searchable.contains("service") || searchable.contains("usecase")
        || searchable.contains("use-case") || searchable.contains("workflow")
        || searchable.contains("process") || searchable.contains("orchestrat")
        || searchable.contains("businessrule") || searchable.contains("flow");
  }

  private boolean isMonitoringEvidence(CodeSignalEvidence evidence) {
    String searchable = String.join(" ", normalized(evidence.matchText()),
        normalized(evidence.ruleId()), normalized(evidence.filePath()),
        normalized(evidence.tags()));
    return searchable.contains("actuator") || searchable.contains("health")
        || searchable.contains("readiness") || searchable.contains("liveness")
        || searchable.contains("observability") || searchable.contains("monitoring");
  }

  private String normalized(String value) {
    return value == null ? "" : value.toLowerCase(Locale.ROOT);
  }

  private String normalized(List<String> values) {
    return values == null ? "" : String.join(" ", values).toLowerCase(Locale.ROOT);
  }
}
