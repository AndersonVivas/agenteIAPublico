package com.pichincha.cdemsaspopenaidocumentation.service;

import com.pichincha.cdemsaspopenaidocumentation.domain.RepositoryCloneRequest;
import com.pichincha.cdemsaspopenaidocumentation.domain.RepositoryCloneResponse;

public interface RepositoryProcessingOrchestrator {

  RepositoryCloneResponse process(RepositoryCloneRequest request);
}