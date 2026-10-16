package com.pichincha.cdemsaspopenaidocumentation.service;

import com.pichincha.cdemsaspopenaidocumentation.domain.RepositoryCloneRequest;
import com.pichincha.cdemsaspopenaidocumentation.domain.RepositoryCloneResponse;

public interface RepositoryProcessingOrchestrator {

  /**
   * Runs the documentation workflow in Temporal for the repository: incremental scan of the
   * pull request and, when requested, generation of every requested (or active) template.
   */
  RepositoryCloneResponse process(RepositoryCloneRequest request, boolean generateDocumentation);
}
