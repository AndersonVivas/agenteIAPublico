package com.pichincha.cdemsaspopenaidocumentation.activity;

import com.pichincha.cdemsaspopenaidocumentation.domain.RepositoryCloneRequest;
import com.pichincha.cdemsaspopenaidocumentation.domain.RepositoryCloneResult;
import io.temporal.activity.ActivityMethod;

public interface CloneRepositoryActivity {

  @ActivityMethod
  RepositoryCloneResult cloneRepository(RepositoryCloneRequest request);
}