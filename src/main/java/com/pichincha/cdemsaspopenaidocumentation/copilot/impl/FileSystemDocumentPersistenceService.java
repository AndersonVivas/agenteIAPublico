package com.pichincha.cdemsaspopenaidocumentation.copilot.impl;

import com.pichincha.cdemsaspopenaidocumentation.copilot.DocumentPersistenceService;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import org.springframework.stereotype.Component;

@Component
public class FileSystemDocumentPersistenceService implements DocumentPersistenceService {

  @Override
  public Path save(String repositoryPath, String outputDirectory, String documentName,
      String content, boolean overwriteExisting) {
    Path directory = Path.of(repositoryPath).resolve(outputDirectory);
    Path filePath = directory.resolve(documentName);
    createDirectories(directory);
    if (Files.exists(filePath) && !overwriteExisting) {
      throw new IllegalStateException("Document already exists: " + filePath);
    }
    writeFile(filePath, content);
    return filePath;
  }

  private void createDirectories(Path directory) {
    try {
      Files.createDirectories(directory);
    } catch (IOException exception) {
      throw new IllegalStateException("Unable to create documentation directory.", exception);
    }
  }

  private void writeFile(Path filePath, String content) {
    try {
      Files.writeString(filePath, content, StandardCharsets.UTF_8,
          StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    } catch (IOException exception) {
      throw new IllegalStateException("Unable to persist generated documentation.", exception);
    }
  }
}
