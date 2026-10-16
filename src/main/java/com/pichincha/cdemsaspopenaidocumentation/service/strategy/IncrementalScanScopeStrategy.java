package com.pichincha.cdemsaspopenaidocumentation.service.strategy;

import com.pichincha.cdemsaspopenaidocumentation.domain.FileChange;
import com.pichincha.cdemsaspopenaidocumentation.domain.ScanPlan;
import com.pichincha.cdemsaspopenaidocumentation.domain.ScanScopeContext;
import com.pichincha.cdemsaspopenaidocumentation.domain.enums.ScanScopeEnum;
import com.pichincha.cdemsaspopenaidocumentation.helper.ManifestDiffCalculator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
@RequiredArgsConstructor
public class IncrementalScanScopeStrategy implements ScanScopeStrategy {

  private static final String REASON = "BASELINE_DIFF";

  private final ManifestDiffCalculator diffCalculator;

  @Override
  public boolean supports(ScanScopeContext context) {
    return !context.forceFullScan() && context.hasCompatibleBaseline();
  }

  @Override
  public ScanPlan plan(ScanScopeContext context) {
    List<FileChange> changes = diffCalculator.diff(context.baselineManifest(),
        context.currentManifest());
    List<String> filesToScan = changes.stream().filter(FileChange::requiresScan)
        .map(FileChange::path).toList();
    return new ScanPlan(ScanScopeEnum.INCREMENTAL, REASON, filesToScan, changes);
  }
}
