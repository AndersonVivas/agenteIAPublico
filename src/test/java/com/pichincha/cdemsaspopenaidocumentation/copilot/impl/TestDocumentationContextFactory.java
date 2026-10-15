package com.pichincha.cdemsaspopenaidocumentation.copilot.impl;

import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentationGenerationContext;
import com.pichincha.cdemsaspopenaidocumentation.domain.CodeScanSummary;
import com.pichincha.cdemsaspopenaidocumentation.domain.CodeSignalEvidence;
import java.time.Instant;
import java.util.List;
import java.util.Map;

final class TestDocumentationContextFactory {
  private TestDocumentationContextFactory() { }
  static DocumentationGenerationContext context() {
    CodeScanSummary summary = new CodeScanSummary("scan", "fp", Instant.now(), 0, 0, 0, 0,
        0, Map.of(), Map.of(), List.of(), List.of(), List.of(), List.of(
            evidence("api", "API"), evidence("exception", "EXCEPTION"),
            evidence("dependency", "DEPENDENCY")), null);
    return new DocumentationGenerationContext("scan", "repo", "/tmp/repo", "org", "project",
        1L, summary);
  }
  private static CodeSignalEvidence evidence(String id, String type) {
    return new CodeSignalEvidence(id, "scan", type, "code", "java", "spring", "A.java", "a",
        "a", "rule", "rule", type, "medium", "runtime", List.of("app"), "cause", "action",
        1, 1, 1, List.of("line"), 1.0, List.of());
  }
}