package com.pichincha.cdemsaspopenaidocumentation.documentation.template.adapter.filesystem;

import com.pichincha.cdemsaspopenaidocumentation.documentation.config.DocumentationTemplatesProperties;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;

@Component
public class TemplateDirectoryScanner {

  private final DocumentationTemplatesProperties properties;

  public TemplateDirectoryScanner(DocumentationTemplatesProperties properties) {
    this.properties = properties;
  }

  public Path rootPath() {
    return Path.of(properties.path());
  }

  public List<Path> scan() {
    if (!properties.enabled() || !Files.isDirectory(rootPath())) {
      return List.of();
    }
    try (Stream<Path> stream = Files.walk(rootPath())) {
      return stream.filter(Files::isRegularFile).filter(this::isSupported).toList();
    } catch (IOException exception) {
      return List.of();
    }
  }

  private boolean isSupported(Path path) {
    String fileName = path.getFileName().toString().toLowerCase(Locale.ROOT);
    return properties.extensions().stream().anyMatch(fileName::endsWith);
  }
}