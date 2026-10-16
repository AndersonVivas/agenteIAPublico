package com.pichincha.cdemsaspopenaidocumentation.copilot.impl;

import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentationGenerationContext;
import com.pichincha.cdemsaspopenaidocumentation.domain.CodeSignalEvidence;
import com.pichincha.cdemsaspopenaidocumentation.domain.FileChange;
import com.pichincha.cdemsaspopenaidocumentation.domain.RepositoryCoordinates;
import com.pichincha.cdemsaspopenaidocumentation.domain.ScanExecutionSummary;
import com.pichincha.cdemsaspopenaidocumentation.domain.enums.FileChangeTypeEnum;
import com.pichincha.cdemsaspopenaidocumentation.domain.enums.ScanScopeEnum;
import java.time.Instant;
import java.util.List;

final class TestDocumentationContextFactory {

  static final RepositoryCoordinates REPOSITORY =
      new RepositoryCoordinates("org", "project", "repo");

  private TestDocumentationContextFactory() {
  }

  static DocumentationGenerationContext context() {
    return context(List.of(evidence("EV-api", "API"), evidence("EV-exception", "EXCEPTION"),
        evidence("EV-dependency", "DEPENDENCY")));
  }

  static DocumentationGenerationContext context(List<CodeSignalEvidence> evidences) {
    ScanExecutionSummary scan = new ScanExecutionSummary("scan-1", ScanScopeEnum.INCREMENTAL,
        "BASELINE_DIFF", "refs/pull/7/merge", "abc123", true, "2.3.0", 1, 1,
        evidences.size(), 0, Instant.parse("2026-10-16T10:15:30Z"));
    return new DocumentationGenerationContext(REPOSITORY, 7L, scan, evidences,
        List.of(new FileChange("src/A.java", FileChangeTypeEnum.MODIFIED)));
  }

  static CodeSignalEvidence evidence(String id, String type) {
    return new CodeSignalEvidence(id, "scan-1", type, "code", "java", "spring", "A.java", "a",
        "a", "rule", "rule", type, "medium", "runtime", List.of("app"), "cause", "action",
        1, 1, 1, List.of("line"), 1.0, List.of());
  }
}
