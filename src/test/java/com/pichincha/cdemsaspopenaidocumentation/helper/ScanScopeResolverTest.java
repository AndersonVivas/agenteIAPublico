package com.pichincha.cdemsaspopenaidocumentation.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.pichincha.cdemsaspopenaidocumentation.domain.FileManifest;
import com.pichincha.cdemsaspopenaidocumentation.domain.ScanPlan;
import com.pichincha.cdemsaspopenaidocumentation.domain.ScanScopeContext;
import com.pichincha.cdemsaspopenaidocumentation.domain.enums.ScanScopeEnum;
import com.pichincha.cdemsaspopenaidocumentation.service.strategy.FullScanScopeStrategy;
import com.pichincha.cdemsaspopenaidocumentation.service.strategy.IncrementalScanScopeStrategy;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ScanScopeResolverTest {

  private static final FileManifest BASELINE = new FileManifest(Map.of("a.java", "1",
      "b.java", "2"));
  private static final FileManifest CURRENT = new FileManifest(Map.of("a.java", "1",
      "b.java", "20", "c.java", "3"));

  private final ManifestDiffCalculator diffCalculator = new ManifestDiffCalculator();
  private final ScanScopeResolver resolver = new ScanScopeResolver(List.of(
      new IncrementalScanScopeStrategy(diffCalculator), new FullScanScopeStrategy(diffCalculator)));

  @Test
  void resolveShouldScanOnlyChangedFilesWhenBaselineIsCompatible() {
    ScanPlan plan = resolver.resolve(context(BASELINE, "2.3.0", false));

    assertEquals(ScanScopeEnum.INCREMENTAL, plan.scope());
    assertEquals(List.of("b.java", "c.java"), plan.filesToScan());
    assertEquals(List.of("b.java"), plan.invalidatedFiles());
  }

  @Test
  void resolveShouldScanEverythingWhenThereIsNoBaseline() {
    ScanPlan plan = resolver.resolve(new ScanScopeContext(CURRENT, FileManifest.empty(), null,
        "2.3.0", false));

    assertEquals(ScanScopeEnum.FULL, plan.scope());
    assertEquals("NO_BASELINE", plan.reason());
    assertEquals(List.of("a.java", "b.java", "c.java"), plan.filesToScan());
  }

  @Test
  void resolveShouldScanEverythingWhenAnalyzerVersionChanged() {
    ScanPlan plan = resolver.resolve(context(BASELINE, "2.2.0", false));

    assertEquals(ScanScopeEnum.FULL, plan.scope());
    assertEquals("ANALYZER_UPGRADED", plan.reason());
  }

  @Test
  void resolveShouldScanEverythingWhenFullScanIsForced() {
    ScanPlan plan = resolver.resolve(context(BASELINE, "2.3.0", true));

    assertEquals(ScanScopeEnum.FULL, plan.scope());
    assertEquals("FORCED", plan.reason());
    assertEquals(2, plan.changes().size());
  }

  private ScanScopeContext context(FileManifest baseline, String baselineVersion,
      boolean forceFullScan) {
    return new ScanScopeContext(CURRENT, baseline, baselineVersion, "2.3.0", forceFullScan);
  }
}
