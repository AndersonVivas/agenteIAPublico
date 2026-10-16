package com.pichincha.cdemsaspopenaidocumentation.activity.impl;

import com.pichincha.cdemsaspopenaidocumentation.activity.ScanRepositoryActivity;
import com.pichincha.cdemsaspopenaidocumentation.domain.ScanCommand;
import com.pichincha.cdemsaspopenaidocumentation.domain.ScanExecutionSummary;
import com.pichincha.cdemsaspopenaidocumentation.service.RepositoryScanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScanRepositoryActivityImpl implements ScanRepositoryActivity {

  private final RepositoryScanService repositoryScanService;

  @Override
  public ScanExecutionSummary scanRepository(ScanCommand command) {
    return repositoryScanService.scan(command);
  }
}
