package com.pichincha.cdemsaspopenaidocumentation.copilot.impl;

import com.pichincha.cdemsaspopenaidocumentation.copilot.DocumentPersistenceService;
import com.pichincha.cdemsaspopenaidocumentation.copilot.config.DocumentationProperties;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FileSystemDocumentPersistenceService implements DocumentPersistenceService {

  private final DocumentationProperties documentationProperties;

  @Override
  public Path save(Path relativePath, String content) {
    Path target = documentationProperties.outputRoot().resolve(relativePath);
    try {
      Files.createDirectories(target.getParent());
      Files.writeString(target, content, StandardCharsets.UTF_8);
      return target;
    } catch (IOException exception) {
      throw new IllegalStateException("Unable to persist documentation file " + target,
          exception);
    }
  }
}
