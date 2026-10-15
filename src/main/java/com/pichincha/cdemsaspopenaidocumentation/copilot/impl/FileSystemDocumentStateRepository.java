package com.pichincha.cdemsaspopenaidocumentation.copilot.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pichincha.cdemsaspopenaidocumentation.copilot.DocumentStateRepository;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentStateSnapshot;
import com.pichincha.cdemsaspopenaidocumentation.copilot.support.DocumentationDirectoryResolver;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Persists the per-document section fingerprint snapshot as a JSON sidecar file next to
 * the generated document, named {@code {documentName}.state.json}.
 */
@Component
public class FileSystemDocumentStateRepository implements DocumentStateRepository {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Override
  public Optional<DocumentStateSnapshot> load(String repositoryPath, String outputDirectory,
      String centralizedOutputPath, String documentName) {
    Path statePath = resolveStatePath(repositoryPath, outputDirectory, centralizedOutputPath,
        documentName);
    if (!Files.isRegularFile(statePath)) {
      return Optional.empty();
    }
    try {
      return Optional.of(MAPPER.readValue(statePath.toFile(), DocumentStateSnapshot.class));
    } catch (IOException exception) {
      return Optional.empty();
    }
  }

  @Override
  public void save(String repositoryPath, String outputDirectory, String centralizedOutputPath,
      String documentName, DocumentStateSnapshot snapshot) {
    Path statePath = resolveStatePath(repositoryPath, outputDirectory, centralizedOutputPath,
        documentName);
    try {
      Files.createDirectories(statePath.getParent());
      MAPPER.writerWithDefaultPrettyPrinter().writeValue(statePath.toFile(), snapshot);
    } catch (IOException exception) {
      throw new IllegalStateException("Unable to persist document state snapshot.", exception);
    }
  }

  private Path resolveStatePath(String repositoryPath, String outputDirectory,
      String centralizedOutputPath, String documentName) {
    Path directory = DocumentationDirectoryResolver.resolve(repositoryPath, outputDirectory,
        centralizedOutputPath);
    return directory.resolve(documentName + ".state.json");
  }
}
