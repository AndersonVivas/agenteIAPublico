package com.pichincha.cdemsaspopenaidocumentation.service.strategy;

import com.pichincha.cdemsaspopenaidocumentation.domain.ScanPlan;
import com.pichincha.cdemsaspopenaidocumentation.domain.ScanScopeContext;

public interface ScanScopeStrategy {

  boolean supports(ScanScopeContext context);

  ScanPlan plan(ScanScopeContext context);
}
