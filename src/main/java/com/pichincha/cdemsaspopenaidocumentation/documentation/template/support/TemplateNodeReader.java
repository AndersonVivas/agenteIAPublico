package com.pichincha.cdemsaspopenaidocumentation.documentation.template.support;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.apache.commons.lang3.StringUtils;

public final class TemplateNodeReader {

  private TemplateNodeReader() {
  }

  public static String text(JsonNode node, String... fieldNames) {
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

  public static String textOrDefault(JsonNode node, String fieldName, String fallback) {
    return StringUtils.defaultIfBlank(text(node, fieldName), fallback);
  }

  public static List<String> stringListOrDefault(JsonNode node, String fieldName,
      List<String> fallback) {
    return node != null && node.has(fieldName) ? stringList(node, fieldName) : fallback;
  }

  public static List<String> stringList(JsonNode parent, String fieldName) {
    return stringList(parent == null ? null : parent.get(fieldName));
  }

  public static List<String> stringList(JsonNode node) {
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

  public static <E extends Enum<E>> E enumValue(JsonNode node, String fieldName,
      Class<E> type, List<String> errors) {
    String value = text(node, fieldName);
    if (StringUtils.isBlank(value)) {
      return null;
    }
    try {
      return Enum.valueOf(type, value.toUpperCase(Locale.ROOT));
    } catch (IllegalArgumentException exception) {
      errors.add("Unsupported value '" + value + "' for " + fieldName + ".");
      return null;
    }
  }
}
