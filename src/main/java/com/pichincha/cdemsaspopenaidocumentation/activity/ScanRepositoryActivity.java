package com.pichincha.cdemsaspopenaidocumentation.activity;

import com.pichincha.cdemsaspopenaidocumentation.domain.CodeScanSummary;
import io.temporal.activity.ActivityMethod;

public interface ScanRepositoryActivity {

  @ActivityMethod
  CodeScanSummary scanRepository(String localRepositoryPath);
}
