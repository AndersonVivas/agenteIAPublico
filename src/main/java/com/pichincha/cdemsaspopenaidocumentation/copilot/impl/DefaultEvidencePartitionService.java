package com.pichincha.cdemsaspopenaidocumentation.copilot.impl;

import com.pichincha.cdemsaspopenaidocumentation.copilot.EvidencePartitionService;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.ApiEvidenceSet;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.BusinessFlowEvidenceSet;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.ConfigurationEvidenceSet;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DependencyEvidenceSet;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.EvidencePartitions;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.ExceptionEvidenceSet;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.MonitoringEvidenceSet;
import com.pichincha.cdemsaspopenaidocumentation.domain.CodeScanSummary;
import com.pichincha.cdemsaspopenaidocumentation.domain.CodeSignalEvidence;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class DefaultEvidencePartitionService implements EvidencePartitionService {

  @Override
  public EvidencePartitions partition(CodeScanSummary summary) {
    List<CodeSignalEvidence> api = new ArrayList<>();
    List<CodeSignalEvidence> exceptions = new ArrayList<>();
    List<CodeSignalEvidence> dependencies = new ArrayList<>();
    List<CodeSignalEvidence> configuration = new ArrayList<>();
    List<CodeSignalEvidence> monitoring = new ArrayList<>();
    List<CodeSignalEvidence> businessFlow = new ArrayList<>();
    evidences(summary).forEach(evidence -> assign(evidence, api, exceptions, dependencies,
        configuration, monitoring, businessFlow));
    return new EvidencePartitions(new ApiEvidenceSet(sorted(api)),
        new ExceptionEvidenceSet(sorted(exceptions)), new DependencyEvidenceSet(sorted(dependencies)),
        new ConfigurationEvidenceSet(sorted(configuration)), new MonitoringEvidenceSet(sorted(monitoring)),
        new BusinessFlowEvidenceSet(sorted(businessFlow)));
  }

  private void assign(CodeSignalEvidence evidence, List<CodeSignalEvidence> api,
      List<CodeSignalEvidence> exceptions, List<CodeSignalEvidence> dependencies,
      List<CodeSignalEvidence> configuration, List<CodeSignalEvidence> monitoring,
      List<CodeSignalEvidence> businessFlow) {
    String signalType = normalized(evidence.signalType());
    if (signalType.equals("api") && isBusinessFlowEvidence(evidence)) {
      businessFlow.add(evidence);
    } else if (signalType.equals("api")) {
      api.add(evidence);
    } else if (signalType.equals("exception")) {
      exceptions.add(evidence);
    } else if (signalType.equals("dependency") || signalType.equals("database")) {
      dependencies.add(evidence);
    } else if (isMonitoringEvidence(evidence)) {
      monitoring.add(evidence);
    } else {
      configuration.add(evidence);
    }
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

  private List<CodeSignalEvidence> evidences(CodeScanSummary summary) {
    return summary == null || summary.evidences() == null ? List.of() : summary.evidences();
  }

  private List<CodeSignalEvidence> sorted(List<CodeSignalEvidence> evidences) {
    return evidences.stream().sorted(Comparator.comparing(CodeSignalEvidence::evidenceId))
        .toList();
  }

  private boolean isMonitoringEvidence(CodeSignalEvidence evidence) {
    String searchable = String.join(" ", normalized(evidence.matchText()),
        normalized(evidence.ruleId()), normalized(evidence.filePath()), normalized(evidence.tags()));
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