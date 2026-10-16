package com.pichincha.cdemsaspopenaidocumentation.helper;

import com.pichincha.cdemsaspopenaidocumentation.domain.ScanPlan;
import com.pichincha.cdemsaspopenaidocumentation.domain.ScanScopeContext;
import com.pichincha.cdemsaspopenaidocumentation.service.strategy.ScanScopeStrategy;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScanScopeResolver {

  private final List<ScanScopeStrategy> strategies;

  public ScanPlan resolve(ScanScopeContext context) {
    return strategies.stream()
        .filter(strategy -> strategy.supports(context))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("No scan scope strategy applies."))
        .plan(context);
  }
}
