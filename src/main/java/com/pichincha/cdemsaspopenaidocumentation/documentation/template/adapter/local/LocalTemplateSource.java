package com.pichincha.cdemsaspopenaidocumentation.documentation.template.adapter.local;

import com.pichincha.cdemsaspopenaidocumentation.documentation.config.TemplatePlatformProperties;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.RawTemplateDescriptor;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateFormat;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateSourceType;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateVersion;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.port.ExternalTemplateSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class LocalTemplateSource implements ExternalTemplateSource {

  private final TemplatePlatformProperties properties;

  public LocalTemplateSource(TemplatePlatformProperties properties) {
    this.properties = properties;
  }

  @Override
  public TemplateSourceType sourceType() {
    return TemplateSourceType.LOCAL;
  }

  @Override
  public List<RawTemplateDescriptor> listAvailable() {
    if (!isEnabled()) {
      return List.of();
    }
    Path root = rootPath();
    if (!Files.isDirectory(root)) {
      return List.of();
    }
    try (Stream<Path> stream = Files.walk(root)) {
      return stream.filter(Files::isRegularFile).filter(this::isSupported)
          .map(path -> toDescriptor(root, path)).flatMap(Optional::stream).toList();
    } catch (IOException exception) {
      return List.of();
    }
  }

  @Override
  public Optional<RawTemplateDescriptor> findBySourceId(String sourceId) {
    return listAvailable().stream().filter(descriptor ->
        descriptor.sourceId().equals(sourceId)).findFirst();
  }

  private boolean isEnabled() {
    return properties.localSourceEnabled()
        && StringUtils.isNotBlank(properties.localSourcePath());
  }

  private Path rootPath() {
    return Path.of(properties.localSourcePath());
  }

  private boolean isSupported(Path path) {
    String fileName = path.getFileName().toString().toLowerCase(Locale.ROOT);
    return properties.localSourceExtensions().stream().anyMatch(fileName::endsWith);
  }

  private Optional<RawTemplateDescriptor> toDescriptor(Path root, Path path) {
    try {
      String content = Files.readString(path);
      String sourceId = root.relativize(path).toString();
      String templateId = fileBaseName(path.getFileName().toString());
      return Optional.of(new RawTemplateDescriptor(sourceId, templateId, "",
          TemplateVersion.initial(), content, format(path), sourceType(),
          path.toAbsolutePath().toString(), "", modifiedAt(path)));
    } catch (IOException exception) {
      return Optional.empty();
    }
  }

  private String fileBaseName(String fileName) {
    int index = fileName.lastIndexOf('.');
    return index < 0 ? fileName : fileName.substring(0, index);
  }

  private TemplateFormat format(Path path) {
    String fileName = path.getFileName().toString().toLowerCase(Locale.ROOT);
    if (fileName.endsWith(".yaml") || fileName.endsWith(".yml")) {
      return TemplateFormat.YAML;
    }
    if (fileName.endsWith(".json")) {
      return TemplateFormat.JSON;
    }
    if (fileName.endsWith(".md")) {
      return TemplateFormat.MARKDOWN;
    }
    return TemplateFormat.UNKNOWN;
  }

  private Instant modifiedAt(Path path) {
    try {
      return Files.getLastModifiedTime(path).toInstant();
    } catch (IOException exception) {
      return Instant.EPOCH;
    }
  }
}
