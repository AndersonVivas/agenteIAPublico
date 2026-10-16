package com.pichincha.cdemsaspopenaidocumentation.helper;

import com.pichincha.cdemsaspopenaidocumentation.domain.SourceFile;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SourceFileReader {

  private static final long MAX_READABLE_FILE_BYTES = 2_000_000;

  private final FileTypeResolver fileTypeResolver;

  public Optional<SourceFile> read(Path repositoryRoot, String relativePath) {
    Path path = repositoryRoot.resolve(relativePath);
    try {
      if (Files.size(path) > MAX_READABLE_FILE_BYTES) {
        return Optional.empty();
      }
      String extension = fileTypeResolver.extension(relativePath);
      return Optional.of(new SourceFile(relativePath, path.getFileName().toString(), extension,
          fileTypeResolver.language(extension), fileTypeResolver.sourceKind(extension),
          Files.readAllLines(path, StandardCharsets.UTF_8)));
    } catch (IOException exception) {
      log.debug("Skipping unreadable file {}: {}", relativePath, exception.getMessage());
      return Optional.empty();
    }
  }
}
