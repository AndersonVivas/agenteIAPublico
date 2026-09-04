package com.pichincha.cdemsaspopenaidocumentation.service;

import com.pichincha.cdemsaspopenaidocumentation.domain.CodeScanSummary;

public interface CodeScanService {

  CodeScanSummary scanRepository(String localRepositoryPath);
}