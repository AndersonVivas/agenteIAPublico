package com.pichincha.cdemsaspopenaidocumentation.service;

import com.pichincha.cdemsaspopenaidocumentation.domain.ScanCommand;
import com.pichincha.cdemsaspopenaidocumentation.domain.ScanExecutionSummary;

public interface RepositoryScanService {

  /**
   * Clones the repository (pull request merge ref when available), scans only the files that
   * changed since the documented baseline, or every file when no compatible baseline exists,
   * and stores the evidence. Repeating a scan identifier returns the stored summary.
   */
  ScanExecutionSummary scan(ScanCommand command);
}
