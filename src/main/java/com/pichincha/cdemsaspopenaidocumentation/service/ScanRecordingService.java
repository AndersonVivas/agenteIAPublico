package com.pichincha.cdemsaspopenaidocumentation.service;

import com.pichincha.cdemsaspopenaidocumentation.domain.ScanExecutionSummary;
import com.pichincha.cdemsaspopenaidocumentation.domain.ScanRecord;

public interface ScanRecordingService {

  /**
   * Atomically supersedes the evidence of changed files, stores the new evidence, updates the
   * file baseline of the repository and registers the scan execution.
   */
  ScanExecutionSummary record(ScanRecord scanRecord);
}
