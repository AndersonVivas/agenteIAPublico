package com.pichincha.cdemsaspopenaidocumentation.activity;

import com.pichincha.cdemsaspopenaidocumentation.domain.ScanCommand;
import com.pichincha.cdemsaspopenaidocumentation.domain.ScanExecutionSummary;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface ScanRepositoryActivity {

  @ActivityMethod
  ScanExecutionSummary scanRepository(ScanCommand command);
}
