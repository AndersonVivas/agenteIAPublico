package com.pichincha.cdemsaspopenaidocumentation.copilot.impl;

import com.pichincha.cdemsaspopenaidocumentation.copilot.ChangelogService;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.ChangelogEntry;
import com.pichincha.cdemsaspopenaidocumentation.copilot.support.DocumentationDirectoryResolver;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;

/**
 * Appends every execution's detected changes to a {@code CHANGELOG.md} file kept
 * separate from the main generated document, as required by the evolutionary
 * documentation strategy.
 */
@Component
public class FileSystemChangelogService implements ChangelogService {

  private static final String CHANGELOG_FILE_NAME = "CHANGELOG.md";

  @Override
  public void record(String repositoryPath, String outputDirectory,
      String centralizedOutputPath, ChangelogEntry entry) {
    Path directory = DocumentationDirectoryResolver.resolve(repositoryPath, outputDirectory,
        centralizedOutputPath);
    Path changelogPath = directory.resolve(CHANGELOG_FILE_NAME);
    try {
      Files.createDirectories(directory);
      ensureHeader(changelogPath);
      Files.writeString(changelogPath, renderEntry(entry), StandardCharsets.UTF_8,
          StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    } catch (IOException exception) {
      throw new IllegalStateException("Unable to append to the documentation changelog.",
          exception);
    }
  }

  private void ensureHeader(Path changelogPath) throws IOException {
    if (!Files.exists(changelogPath)) {
      Files.writeString(changelogPath, "# Changelog\n\n", StandardCharsets.UTF_8,
          StandardOpenOption.CREATE);
    }
  }

  private String renderEntry(ChangelogEntry entry) {
    StringBuilder builder = new StringBuilder();
    builder.append("## ")
        .append(DateTimeFormatter.ISO_INSTANT.format(entry.timestamp()))
        .append(" - ").append(entry.repositoryName())
        .append(" (").append(entry.commitHash()).append(")\n");
    if (entry.pullRequestId() != null) {
      builder.append("- Pull Request: #").append(entry.pullRequestId()).append('\n');
    }
    appendList(builder, "Cambios detectados", entry.detectedChanges());
    appendList(builder, "Secciones actualizadas", entry.updatedSections());
    appendList(builder, "Nueva evidencia", entry.newEvidenceIds());
    builder.append('\n');
    return builder.toString();
  }

  private void appendList(StringBuilder builder, String label, Iterable<String> values) {
    builder.append("- ").append(label).append(": ");
    boolean hasAny = false;
    StringBuilder joined = new StringBuilder();
    for (String value : values) {
      if (hasAny) {
        joined.append(", ");
      }
      joined.append(value);
      hasAny = true;
    }
    builder.append(hasAny ? joined : "ninguno").append('\n');
  }
}
