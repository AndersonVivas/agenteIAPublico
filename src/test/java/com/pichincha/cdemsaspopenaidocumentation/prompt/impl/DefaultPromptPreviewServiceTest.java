package com.pichincha.cdemsaspopenaidocumentation.prompt.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pichincha.cdemsaspopenaidocumentation.domain.CodeScanSummary;
import com.pichincha.cdemsaspopenaidocumentation.domain.RepositoryCloneRequest;
import com.pichincha.cdemsaspopenaidocumentation.domain.ScanExecutionMetadata;
import com.pichincha.cdemsaspopenaidocumentation.prompt.PromptBuilderService;
import com.pichincha.cdemsaspopenaidocumentation.prompt.domain.PromptContext;
import com.pichincha.cdemsaspopenaidocumentation.prompt.domain.PromptType;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class DefaultPromptPreviewServiceTest {

  @Test
  void shouldDescribeScanSummaryByLanguageDistribution() {
    PromptBuilderService builder = new CapturingPromptBuilderService();
    DefaultPromptPreviewService service = new DefaultPromptPreviewService(builder);
    CodeScanSummary summary = new CodeScanSummary(
        "SCAN-123",
        "REPO-ABC",
        java.time.Instant.now(),
        6, 0, 1, 1, 2,
        Map.of("python", 3L, "swift", 2L, "yaml", 1L),
        Map.of("py", 3L, "swift", 2L, "yml", 1L),
        List.of(), List.of(), List.of(), List.of(),
        new ScanExecutionMetadata("REPO-ABC", false, "", "HEAD", "FULL", "v1", "2.2.0",
            10L, 6L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 100L));

    String prompt = service.buildPreview(request(), summary, "/tmp/repo", PromptType.ERROR_MANUAL);

    assertTrue(prompt.contains("languages=python:3, swift:2, yaml:1"));
    assertTrue(prompt.contains("extensions=py:3, swift:2, yml:1"));
    assertFalse(prompt.contains("javaFiles="));
  }

  private RepositoryCloneRequest request() {
    return new RepositoryCloneRequest("org", "proj", "repo", 1L);
  }

  private static final class CapturingPromptBuilderService implements PromptBuilderService {

    @Override
    public String buildPrompt(PromptType promptType, PromptContext context) {
      return context.ragContext();
    }
  }
}
