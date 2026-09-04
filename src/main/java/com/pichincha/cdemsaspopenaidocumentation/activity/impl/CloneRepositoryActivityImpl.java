package com.pichincha.cdemsaspopenaidocumentation.activity.impl;

import com.pichincha.cdemsaspopenaidocumentation.activity.CloneRepositoryActivity;
import com.pichincha.cdemsaspopenaidocumentation.domain.RepositoryCloneRequest;
import com.pichincha.cdemsaspopenaidocumentation.domain.RepositoryCloneResult;
import com.pichincha.cdemsaspopenaidocumentation.helper.GitRepositoryCloner;
import org.springframework.stereotype.Component;

@Component
public class CloneRepositoryActivityImpl implements CloneRepositoryActivity {

  private final GitRepositoryCloner gitRepositoryCloner;

  public CloneRepositoryActivityImpl(GitRepositoryCloner gitRepositoryCloner) {
    this.gitRepositoryCloner = gitRepositoryCloner;
  }

  @Override
  public RepositoryCloneResult cloneRepository(RepositoryCloneRequest request) {
    return gitRepositoryCloner.cloneRepository(request);
  }
}