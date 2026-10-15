package com.pichincha.cdemsaspopenaidocumentation.knowledge.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pichincha.cdemsaspopenaidocumentation.domain.CodeScanSummary;
import com.pichincha.cdemsaspopenaidocumentation.domain.CodeSignalEvidence;
import com.pichincha.cdemsaspopenaidocumentation.domain.ScanExecutionMetadata;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.ProjectKnowledgeBase;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class DefaultKnowledgeProjectionServiceTest {

  @Test
  void shouldBuildDeterministicKnowledgeBaseAndDeduplicateEvidence() {
    DefaultKnowledgeProjectionService service = new DefaultKnowledgeProjectionService();
    CodeSignalEvidence api = evidence("api-1", "API", "ApiController.java", "API_ROUTE",
        "@GetMapping /payments", "high");
    CodeSignalEvidence duplicateApi = evidence("api-1", "API", "ApiController.java",
        "API_ROUTE", "@GetMapping /payments", "high");
    CodeSignalEvidence exception = evidence("ex-1", "EXCEPTION", "ErrorHandler.java",
        "EX_HANDLER", "@ExceptionHandler", "medium");
    CodeSignalEvidence dependency = evidence("dep-1", "DEPENDENCY", "build.gradle",
        "DEP_HTTP", "implementation 'org.springframework..." , "low");
    CodeSignalEvidence monitoring = evidence("mon-1", "CONFIGURATION",
        "application.yml", "ACTUATOR_HEALTH", "management.health.readiness", "medium");
    CodeScanSummary summary = summary(List.of(api, duplicateApi, exception, dependency,
        monitoring));

    ProjectKnowledgeBase kb1 = service.project(summary, List.of());
    ProjectKnowledgeBase kb2 = service.project(summary, List.of());

    assertEquals(kb1, kb2);
    assertEquals(4, kb1.evidenceIndex().size());
    assertEquals(1, kb1.apiKnowledge().findings().size());
    assertEquals(1, kb1.exceptionKnowledge().findings().size());
    assertEquals(1, kb1.dependencyKnowledge().findings().size());
    assertEquals(1, kb1.monitoringKnowledge().findings().size());
    assertTrue(kb1.configurationKnowledge().findings().isEmpty());
    assertFalse(kb1.knowledgeGaps().isEmpty());
  }

  @Test
  void shouldDetectSeverityConflictInSameFinding() {
    DefaultKnowledgeProjectionService service = new DefaultKnowledgeProjectionService();
    CodeSignalEvidence high = evidence("dep-1", "DEPENDENCY", "build.gradle",
        "DEP_TIMEOUT", "timeout", "high");
    CodeSignalEvidence low = evidence("dep-2", "DEPENDENCY", "build.gradle",
        "DEP_TIMEOUT", "timeout", "low");
    CodeScanSummary summary = summary(List.of(high, low));

    ProjectKnowledgeBase kb = service.project(summary, List.of());

    assertEquals(1, kb.knowledgeConflicts().size());
    assertTrue(kb.knowledgeConflicts().get(0).description().toLowerCase()
        .contains("inconsistent severity"));
  }

    @Test
    void shouldClassifyMonitoringSignalTypeAsMonitoring() {
        CodeSignalEvidence evidence = evidence("mon-1", "MONITORING", "application.yml",
                "GENERIC_RULE", "custom signal", "medium");

        assertMonitoringClassification(evidence);
    }

    @Test
    void shouldClassifyActuatorAsMonitoring() {
        CodeSignalEvidence evidence = evidence("mon-2", "CONFIGURATION", "application.yml",
                "ACTUATOR_ENDPOINT", "management.endpoints.web.exposure.include=health",
                "medium");

        assertMonitoringClassification(evidence);
    }

    @Test
    void shouldClassifyKafkaAsMonitoring() {
        CodeSignalEvidence evidence = evidence("mon-3", "CONFIGURATION", "application.yml",
                "KAFKA_TOPIC", "spring.kafka.bootstrap-servers=localhost:9092", "medium");

        assertMonitoringClassification(evidence);
    }

    @Test
    void shouldClassifyLoggingAsMonitoring() {
        CodeSignalEvidence evidence = evidence("mon-4", "CONFIGURATION", "logback.xml",
                "LOGGING_CONFIG", "logging.level.root=INFO", "medium");

        assertMonitoringClassification(evidence);
    }

    @Test
    void shouldClassifyMetricsAsMonitoring() {
        CodeSignalEvidence evidence = evidence("mon-5", "CONFIGURATION", "application.yml",
                "METRICS_EXPORT", "management.metrics.export.prometheus.enabled=true",
                "medium");

        assertMonitoringClassification(evidence);
    }

    @Test
    void shouldClassifyHelmHealthChecksAsMonitoring() {
        CodeSignalEvidence evidence = evidence("mon-6", "CONFIGURATION", "helm/dev.yml",
                "HELM_PROBES", "readinessProbe and livenessProbe configured", "medium",
                List.of("helm", "health-checks"));

        assertMonitoringClassification(evidence);
    }

    @Test
    void shouldNotDuplicateWhenSummaryAndExplicitEvidenceOverlap() {
        DefaultKnowledgeProjectionService service = new DefaultKnowledgeProjectionService();
        CodeSignalEvidence api = evidence("api-1", "API", "ApiController.java", "API_ROUTE",
                "@GetMapping /payments", "high");
        CodeSignalEvidence exception = evidence("ex-1", "EXCEPTION", "ErrorHandler.java",
                "EX_HANDLER", "@ExceptionHandler", "medium");
        CodeScanSummary summary = summary(List.of(api, exception));

        ProjectKnowledgeBase kb = service.project(summary, List.of(api, exception));

        assertEquals(2, kb.evidenceIndex().size());
        assertEquals(1, kb.apiKnowledge().findings().size());
        assertEquals(1, kb.exceptionKnowledge().findings().size());
    }

    private void assertMonitoringClassification(CodeSignalEvidence evidence) {
        DefaultKnowledgeProjectionService service = new DefaultKnowledgeProjectionService();
        ProjectKnowledgeBase kb = service.project(summary(List.of(evidence)), List.of());

        assertEquals(1, kb.monitoringKnowledge().findings().size());
        assertTrue(kb.configurationKnowledge().findings().isEmpty());
    }

  private CodeScanSummary summary(List<CodeSignalEvidence> evidences) {
    ScanExecutionMetadata metadata = new ScanExecutionMetadata("fp", false, "", "",
        "FULL", "2.0", "2.2", 10L, 1L, 0L, 1L, 1L, 1L, 1L, 1L, evidences.size(),
        100L);
    return new CodeScanSummary("scan-1", "fp", Instant.now(), 5, 2, 1, 1, 2,
        Map.of("java", 2L, "yaml", 1L), Map.of("java", 2L, "yml", 1L), List.of(),
        List.of(), List.of(), evidences, metadata);
  }

  private CodeSignalEvidence evidence(String id, String signalType, String path,
      String ruleId, String matchText, String severity) {
        return evidence(id, signalType, path, ruleId, matchText, severity,
                List.of("tag"));
    }

    private CodeSignalEvidence evidence(String id, String signalType, String path,
            String ruleId, String matchText, String severity, List<String> tags) {
    return new CodeSignalEvidence(id, "scan-1", signalType, "code", "java", "spring",
        path, "symbol", "scope", ruleId, ruleId, matchText, severity, "runtime",
        List.of("dep"), "cause", "action", 1, 1, 1, List.of("line"), 0.9,
                tags);
  }
}
