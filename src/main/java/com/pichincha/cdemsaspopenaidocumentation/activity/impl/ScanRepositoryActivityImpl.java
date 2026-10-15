package com.pichincha.cdemsaspopenaidocumentation.activity.impl;

import com.pichincha.cdemsaspopenaidocumentation.activity.ScanRepositoryActivity;
import com.pichincha.cdemsaspopenaidocumentation.domain.CodeScanSummary;
import com.pichincha.cdemsaspopenaidocumentation.service.CodeScanService;
import org.springframework.stereotype.Component;

@Component
public class ScanRepositoryActivityImpl implements ScanRepositoryActivity {

  private final CodeScanService codeScanService;

  public ScanRepositoryActivityImpl(CodeScanService codeScanService) {
    this.codeScanService = codeScanService;
  }

  @Override
  public CodeScanSummary scanRepository(String localRepositoryPath) {
    return codeScanService.scanRepository(localRepositoryPath);
  }
}
