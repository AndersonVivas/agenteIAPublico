package com.pichincha.cdemsaspopenaidocumentation.documentation.template.support;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Lists the template keys that the pipeline does not read, so template authors learn that
 * they have no effect on the generated documentation.
 */
public final class TemplateKeyInspector {

  private static final String OUTPUT_CONTRACT = "outputContract";
  private static final Set<String> TEMPLATE_KEYS = Set.of("id", "templateId", "name",
      "templateName", "title", "version", "status", "description", "lifecycle",
      "editorialReview", "skills", "audience", "audienceProfile", "communicationProfile",
      "globalRules", OUTPUT_CONTRACT, "sections");
  private static final Set<String> OUTPUT_CONTRACT_KEYS = Set.of("missingInformationText");

  private TemplateKeyInspector() {
  }

  public static List<String> unsupportedKeys(JsonNode definition) {
    if (definition == null || !definition.isObject()) {
      return List.of();
    }
    List<String> unsupported = new ArrayList<>(
        unsupported(definition, TEMPLATE_KEYS::contains, ""));
    JsonNode outputContract = definition.get(OUTPUT_CONTRACT);
    if (outputContract != null && outputContract.isObject()) {
      unsupported.addAll(unsupported(outputContract, OUTPUT_CONTRACT_KEYS::contains,
          OUTPUT_CONTRACT + "."));
    }
    return unsupported;
  }

  private static List<String> unsupported(JsonNode node, Predicate<String> supported,
      String prefix) {
    return node.properties().stream()
        .map(Map.Entry::getKey)
        .filter(supported.negate())
        .map(key -> prefix + key)
        .toList();
  }
}
