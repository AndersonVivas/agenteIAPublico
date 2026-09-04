package com.pichincha.cdemsaspopenaidocumentation.service.impl;

import com.pichincha.cdemsaspopenaidocumentation.domain.RepositoryCloneRequest;
import com.pichincha.cdemsaspopenaidocumentation.domain.RepositoryCloneResponse;
import com.pichincha.cdemsaspopenaidocumentation.domain.RepositoryCloneResult;
import com.pichincha.cdemsaspopenaidocumentation.helper.GitRepositoryCloner;
import com.pichincha.cdemsaspopenaidocumentation.service.CodeScanService;
import com.pichincha.cdemsaspopenaidocumentation.service.RepositoryProcessingOrchestrator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.temporal", name = "enabled", havingValue = "false",
    matchIfMissing = true)
public class DirectRepositoryProcessingOrchestrator implements RepositoryProcessingOrchestrator {

  private final GitRepositoryCloner gitRepositoryCloner;
  private final CodeScanService codeScanService;

  public DirectRepositoryProcessingOrchestrator(GitRepositoryCloner gitRepositoryCloner,
      CodeScanService codeScanService) {
    this.gitRepositoryCloner = gitRepositoryCloner;
    this.codeScanService = codeScanService;
  }

  @Override
  public RepositoryCloneResponse process(RepositoryCloneRequest request) {
    RepositoryCloneResult cloneResult = gitRepositoryCloner.cloneRepository(request);
    return RepositoryCloneResponse.direct(cloneResult,
        codeScanService.scanRepository(cloneResult.localPath()));
  }
}