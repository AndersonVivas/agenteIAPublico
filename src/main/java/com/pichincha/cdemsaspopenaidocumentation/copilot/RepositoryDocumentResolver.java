package com.pichincha.cdemsaspopenaidocumentation.copilot;

import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.RepositoryDocumentSnapshot;
import java.util.Optional;

public interface RepositoryDocumentResolver {

  Optional<RepositoryDocumentSnapshot> resolve(String repositoryPath,
      String outputDirectory, String documentName);
}
