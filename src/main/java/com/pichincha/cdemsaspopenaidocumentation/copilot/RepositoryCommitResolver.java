package com.pichincha.cdemsaspopenaidocumentation.copilot;

import java.nio.file.Path;
import java.util.Optional;

public interface RepositoryCommitResolver {

  Optional<String> resolveCommitHash(Path repositoryPath);
}
