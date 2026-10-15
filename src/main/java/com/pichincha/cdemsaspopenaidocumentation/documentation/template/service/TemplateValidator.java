package com.pichincha.cdemsaspopenaidocumentation.documentation.template.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.DocumentTemplate;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.RawTemplateDescriptor;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateFormat;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateMetadata;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateSection;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateValidationResult;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class TemplateValidator {

  private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
  private static final ObjectMapper YAML_MAPPER = new ObjectMapper(new YAMLFactory());
  private static final Pattern FRONT_MATTER = Pattern.compile(
      "^---\\s*\\R(.*?)\\R---\\s*(?:\\R|$)", Pattern.DOTALL);

  public TemplateValidationResult validate(RawTemplateDescriptor descriptor) {
    List<String> errors = new ArrayList<>();
    List<String> warnings = new ArrayList<>();
    if (descriptor == null) {
      errors.add("Template descriptor is required.");
      return TemplateValidationResult.invalid(errors, warnings);
    }
    JsonNode definition = parseDefinition(descriptor, errors, warnings);
    String templateId = normalizeTemplateId(descriptor, definition, warnings);
    String templateName = normalizeTemplateName(descriptor, definition, templateId,
        warnings);
    validateDescriptor(descriptor, templateId, templateName, errors);
    if (!errors.isEmpty()) {
      return TemplateValidationResult.invalid(errors, warnings);
    }
    DocumentTemplate template = new DocumentTemplate(templateId, templateName,
        descriptor.declaredVersion(), sections(descriptor, definition),
        metadata(descriptor, definition));
    return TemplateValidationResult.valid(template, warnings);
  }

  private JsonNode parseDefinition(RawTemplateDescriptor descriptor,
      List<String> errors, List<String> warnings) {
    if (descriptor.format() == TemplateFormat.UNKNOWN) {
      return null;
    }
    return switch (descriptor.format()) {
      case YAML -> readNode(descriptor.content(), YAML_MAPPER,
          "Invalid YAML template definition.", errors);
      case JSON -> readNode(descriptor.content(), JSON_MAPPER,
          "Invalid JSON template definition.", errors);
      case MARKDOWN -> markdownDefinition(descriptor.content(), warnings).orElse(null);
      case UNKNOWN -> null;
    };
  }

  private JsonNode readNode(String content, ObjectMapper mapper, String errorMessage,
      List<String> errors) {
    try {
      return mapper.readTree(StringUtils.defaultString(content));
    } catch (JsonProcessingException exception) {
      errors.add(errorMessage);
      return null;
    }
  }

  private void validateDescriptor(RawTemplateDescriptor descriptor, String templateId,
      String templateName, List<String> errors) {
    if (StringUtils.isBlank(descriptor.sourceId())) {
      errors.add("Source identifier is required.");
    }
    if (StringUtils.isBlank(templateId)) {
      errors.add("Template identifier is required.");
    }
    if (StringUtils.isBlank(templateName)) {
      errors.add("Template name is required.");
    }
    if (descriptor.format() == null || descriptor.format() == TemplateFormat.UNKNOWN) {
      errors.add("Template format is unsupported.");
    }
  }

  private TemplateMetadata metadata(RawTemplateDescriptor descriptor,
      JsonNode definition) {
    Instant publishedAt = descriptor.lastModifiedAt();
    List<String> tags = tags(definition);
    Map<String, String> attributes = Map.of(
        "sourceId", descriptor.sourceId(),
        "format", descriptor.format().name(),
        "checksum", descriptor.checksum());
    return new TemplateMetadata(descriptor.sourceType().name().toLowerCase(),
        descriptor.sourceReference(), publishedAt, tags, attributes);
  }

  private String normalizeTemplateId(RawTemplateDescriptor descriptor,
      JsonNode definition,
      List<String> warnings) {
    if (StringUtils.isNotBlank(descriptor.templateId())) {
      return descriptor.templateId().trim();
    }
    String metadataId = text(definition, "templateId", "id");
    if (StringUtils.isNotBlank(metadataId)) {
      warnings.add("Template id was resolved from template content.");
      return metadataId.trim();
    }
    warnings.add("Template id was not provided. Using source identifier.");
    return sanitize(descriptor.sourceId());
  }

  private String normalizeTemplateName(RawTemplateDescriptor descriptor,
      JsonNode definition, String templateId, List<String> warnings) {
    if (StringUtils.isNotBlank(descriptor.templateName())) {
      return descriptor.templateName().trim();
    }
    String metadataName = text(definition, "templateName", "name", "title");
    if (StringUtils.isNotBlank(metadataName)) {
      warnings.add("Template name was resolved from template content.");
      return metadataName.trim();
    }
    warnings.add("Template name was not provided. Using normalized id.");
    return templateId.replace('-', ' ');
  }

  private List<TemplateSection> sections(RawTemplateDescriptor descriptor,
      JsonNode definition) {
    List<TemplateSection> definedSections = contentSections(definition);
    if (!definedSections.isEmpty()) {
      return definedSections;
    }
    if (descriptor.format() == TemplateFormat.MARKDOWN) {
      return markdownSections(descriptor.content());
    }
    return List.of();
  }

  private List<TemplateSection> contentSections(JsonNode definition) {
    if (definition == null || !definition.has("sections")) {
      return List.of();
    }
    JsonNode sectionsNode = definition.get("sections");
    if (!sectionsNode.isArray()) {
      return List.of();
    }
    List<TemplateSection> sections = new ArrayList<>();
    int order = 1;
    for (JsonNode sectionNode : sectionsNode) {
      sections.add(toSection(sectionNode, order));
      order++;
    }
    return sections;
  }

  private TemplateSection toSection(JsonNode sectionNode, int fallbackOrder) {
    String title = text(sectionNode, "title", "name");
    String id = text(sectionNode, "id");
    String objective = text(sectionNode, "objective", "description");
    List<String> instructions = stringList(sectionNode == null
        ? null : sectionNode.get("instructions"));
    boolean required = sectionNode == null || !sectionNode.has("required")
        || sectionNode.get("required").asBoolean(true);
    int order = sectionNode == null ? fallbackOrder
        : sectionNode.path("order").asInt(fallbackOrder);
    return new TemplateSection(sanitize(StringUtils.defaultIfBlank(id, title)),
        StringUtils.defaultIfBlank(title, "Section " + fallbackOrder),
        StringUtils.defaultIfBlank(objective, title), instructions, required,
        order);
  }

  private List<TemplateSection> markdownSections(String content) {
    List<TemplateSection> sections = new ArrayList<>();
    int order = 1;
    for (String line : markdownBody(content).lines().toList()) {
      if (!line.startsWith("## ")) {
        continue;
      }
      String title = line.substring(3).trim();
      sections.add(new TemplateSection(sanitize(title), title, title, List.of(),
          true, order));
      order++;
    }
    return sections;
  }

  private Optional<JsonNode> markdownDefinition(String content,
      List<String> warnings) {
    var matcher = FRONT_MATTER.matcher(StringUtils.defaultString(content));
    if (!matcher.find()) {
      return Optional.empty();
    }
    try {
      return Optional.ofNullable(YAML_MAPPER.readTree(matcher.group(1)));
    } catch (JsonProcessingException exception) {
      warnings.add("Markdown front matter could not be parsed.");
      return Optional.empty();
    }
  }

  private String markdownBody(String content) {
    var matcher = FRONT_MATTER.matcher(StringUtils.defaultString(content));
    if (!matcher.find()) {
      return StringUtils.defaultString(content);
    }
    return StringUtils.defaultString(content).substring(matcher.end()).trim();
  }

  private List<String> tags(JsonNode definition) {
    return stringList(definition == null ? null : definition.get("tags"));
  }

  private List<String> stringList(JsonNode node) {
    if (node == null) {
      return List.of();
    }
    if (node.isTextual()) {
      return List.of(node.asText());
    }
    if (!node.isArray()) {
      return List.of();
    }
    List<String> values = new ArrayList<>();
    node.forEach(item -> values.add(item.asText()));
    return values.stream().map(StringUtils::defaultString).map(String::trim)
        .filter(StringUtils::isNotBlank).toList();
  }

  private String text(JsonNode node, String... fieldNames) {
    if (node == null) {
      return "";
    }
    for (String fieldName : fieldNames) {
      String value = node.path(fieldName).asText("").trim();
      if (StringUtils.isNotBlank(value)) {
        return value;
      }
    }
    return "";
  }

  private String sanitize(String sourceId) {
    return StringUtils.defaultString(sourceId).trim().toLowerCase()
        .replaceAll("[^a-z0-9-]", "-");
  }
}
