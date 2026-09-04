package com.pichincha.cdemsaspopenaidocumentation.copilot;

import java.nio.file.Path;

public interface DocumentPersistenceService {

  Path save(String repositoryPath, String outputDirectory, String documentName,
      String content, boolean overwriteExisting);
}
