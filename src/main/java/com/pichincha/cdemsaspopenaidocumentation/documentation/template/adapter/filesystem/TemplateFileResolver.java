package com.pichincha.cdemsaspopenaidocumentation.documentation.template.adapter.filesystem;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.RawTemplateDescriptor;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateFormat;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateSourceType;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateVersion;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class TemplateFileResolver {

  private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
  private static final ObjectMapper YAML_MAPPER = new ObjectMapper(new YAMLFactory());
  private static final Pattern FRONT_MATTER = Pattern.compile(
      "^---\\s*\\R(.*?)\\R---\\s*(?:\\R|$)", Pattern.DOTALL);

  private final TemplateVersionResolver versionResolver;

  public TemplateFileResolver(TemplateVersionResolver versionResolver) {
    this.versionResolver = versionResolver;
  }

  public Optional<RawTemplateDescriptor> resolve(Path root, Path path) {
    try {
      String content = Files.readString(path);
      TemplateFormat format = format(path);
      JsonNode metadata = metadataNode(content, format).orElse(null);
      String sourceId = root.relativize(path).toString();
      String templateId = text(metadata, "templateId", "id");
      String templateName = text(metadata, "templateName", "name", "title");
      TemplateVersion version = versionResolver.resolve(path, text(metadata, "version"));
      return Optional.of(new RawTemplateDescriptor(sourceId, templateId, templateName,
          version, content, format, TemplateSourceType.FILE_SYSTEM,
          path.toAbsolutePath().toString(), checksum(content), modifiedAt(path)));
    } catch (IOException exception) {
      return Optional.empty();
    }
  }

  private Optional<JsonNode> metadataNode(String content, TemplateFormat format) {
    return switch (format) {
      case YAML -> readNode(content, YAML_MAPPER);
      case JSON -> readNode(content, JSON_MAPPER);
      case MARKDOWN -> markdownFrontMatter(content).flatMap(this::yamlNode);
      case UNKNOWN -> Optional.empty();
    };
  }

  private Optional<JsonNode> readNode(String content, ObjectMapper mapper) {
    try {
      return Optional.ofNullable(mapper.readTree(StringUtils.defaultString(content)));
    } catch (JsonProcessingException exception) {
      return Optional.empty();
    }
  }

  private Optional<JsonNode> yamlNode(String content) {
    return readNode(content, YAML_MAPPER);
  }

  private Optional<String> markdownFrontMatter(String content) {
    var matcher = FRONT_MATTER.matcher(StringUtils.defaultString(content));
    return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
  }

  private String text(JsonNode node, String... fields) {
    if (node == null) {
      return "";
    }
    for (String field : fields) {
      String value = node.path(field).asText("").trim();
      if (StringUtils.isNotBlank(value)) {
        return value;
      }
    }
    return "";
  }

  private TemplateFormat format(Path path) {
    String fileName = path.getFileName().toString().toLowerCase();
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

  private String checksum(String content) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] value = digest.digest(content.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(value);
    } catch (NoSuchAlgorithmException exception) {
      return "";
    }
  }

  private Instant modifiedAt(Path path) {
    try {
      return Files.getLastModifiedTime(path).toInstant();
    } catch (IOException exception) {
      return Instant.EPOCH;
    }
  }
}