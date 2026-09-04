package com.pichincha.cdemsaspopenaidocumentation.copilot.impl;

import com.pichincha.cdemsaspopenaidocumentation.copilot.RepositoryDocumentResolver;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.RepositoryDocumentSnapshot;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class FileSystemRepositoryDocumentResolver implements RepositoryDocumentResolver {

  private static final Pattern VERSION_PATTERN =
      Pattern.compile("document-version:\\s*(\\d+)", Pattern.CASE_INSENSITIVE);

  @Override
  public Optional<RepositoryDocumentSnapshot> resolve(String repositoryPath,
      String outputDirectory, String documentName) {
    Path filePath = Path.of(repositoryPath).resolve(outputDirectory).resolve(documentName);
    if (!Files.exists(filePath)) {
      return Optional.empty();
    }
    try {
      String content = Files.readString(filePath, StandardCharsets.UTF_8);
      return Optional.of(new RepositoryDocumentSnapshot(filePath, content,
          resolveVersion(content)));
    } catch (IOException exception) {
      throw new IllegalStateException("Unable to read repository documentation.", exception);
    }
  }

  private int resolveVersion(String content) {
    Matcher matcher = VERSION_PATTERN.matcher(content);
    return matcher.find() ? Integer.parseInt(matcher.group(1)) : 0;
  }
}
