package com.pichincha.cdemsaspopenaidocumentation.service.strategy;

import com.pichincha.cdemsaspopenaidocumentation.domain.ScanPlan;
import com.pichincha.cdemsaspopenaidocumentation.domain.ScanScopeContext;
import com.pichincha.cdemsaspopenaidocumentation.domain.enums.ScanScopeEnum;
import com.pichincha.cdemsaspopenaidocumentation.helper.ManifestDiffCalculator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
@RequiredArgsConstructor
public class FullScanScopeStrategy implements ScanScopeStrategy {

  private final ManifestDiffCalculator diffCalculator;

  @Override
  public boolean supports(ScanScopeContext context) {
    return true;
  }

  @Override
  public ScanPlan plan(ScanScopeContext context) {
    List<String> allFiles = context.currentManifest().blobsByPath().keySet().stream()
        .sorted().toList();
    return new ScanPlan(ScanScopeEnum.FULL, reason(context), allFiles,
        diffCalculator.diff(context.baselineManifest(), context.currentManifest()));
  }

  private String reason(ScanScopeContext context) {
    if (context.forceFullScan()) {
      return "FORCED";
    }
    return context.hasBaseline() ? "ANALYZER_UPGRADED" : "NO_BASELINE";
  }
}
