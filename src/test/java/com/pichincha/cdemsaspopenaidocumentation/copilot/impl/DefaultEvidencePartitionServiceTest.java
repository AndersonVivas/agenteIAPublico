package com.pichincha.cdemsaspopenaidocumentation.copilot.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.EvidencePartitions;
import com.pichincha.cdemsaspopenaidocumentation.domain.CodeScanSummary;
import com.pichincha.cdemsaspopenaidocumentation.domain.CodeSignalEvidence;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class DefaultEvidencePartitionServiceTest {

  @Test
  void shouldCreateExclusiveDeterministicEvidencePartitions() {
    EvidencePartitions partitions = new DefaultEvidencePartitionService().partition(summary());

    assertEquals(List.of("api"),
        partitions.api().evidences().stream().map(CodeSignalEvidence::evidenceId).toList());
    assertEquals(List.of("exception"), partitions.exceptions().evidences().stream()
        .map(CodeSignalEvidence::evidenceId).toList());
    assertEquals(List.of("database", "dependency"), partitions.dependencies().evidences().stream()
        .map(CodeSignalEvidence::evidenceId).toList());
    assertEquals(List.of("config"), partitions.configuration().evidences().stream()
        .map(CodeSignalEvidence::evidenceId).toList());
    assertEquals(List.of("monitoring"), partitions.monitoring().evidences().stream()
        .map(CodeSignalEvidence::evidenceId).toList());
    long total = partitions.api().evidences().size() + partitions.exceptions().evidences().size()
        + partitions.dependencies().evidences().size() + partitions.configuration().evidences().size()
        + partitions.monitoring().evidences().size();
    assertEquals(6L, total);
    assertTrue(partitions.monitoring().evidences().stream()
        .allMatch(evidence -> evidence.signalType().equals("CONFIG")));
  }

  private CodeScanSummary summary() {
    return new CodeScanSummary("scan", "fingerprint", Instant.now(), 0, 0, 0, 0, 0,
        Map.of(), Map.of(), List.of(), List.of(), List.of(), List.of(
            evidence("dependency", "DEPENDENCY", "Kafka"),
            evidence("api", "API", "@GetMapping"),
            evidence("monitoring", "CONFIG", "management.health.readinessstate.enabled"),
            evidence("database", "DATABASE", "jdbc:postgresql"),
            evidence("exception", "EXCEPTION", "throw new Error()"),
            evidence("config", "CONFIG", "client.timeout: 2000")), null);
  }

  private CodeSignalEvidence evidence(String evidenceId, String signalType, String matchText) {
    return new CodeSignalEvidence(evidenceId, "scan", signalType, "code", "java", "spring",
        "App.java", "app", "app", "rule", "rule", matchText, "medium", "runtime",
        List.of("application"), "cause", "action", 1, 1, 1, List.of("line"), 1.0, List.of());
  }
}