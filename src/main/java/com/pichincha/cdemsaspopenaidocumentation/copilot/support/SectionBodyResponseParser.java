package com.pichincha.cdemsaspopenaidocumentation.copilot.support;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pichincha.cdemsaspopenaidocumentation.copilot.exception.AgentExecutionException;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

/**
 * Reads the JSON object returned by the agents that write Markdown ({@code sectionId} to
 * section body) and keeps only the non-blank bodies of the requested sections.
 */
public final class SectionBodyResponseParser {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private SectionBodyResponseParser() {
  }

  public static Map<String, String> parse(String rawOutput, Set<String> requestedSectionIds,
      String agentLabel) {
    try {
      String json = JsonResponseExtractor.extractObject(rawOutput);
      Map<String, String> raw = MAPPER.readValue(json, new TypeReference<>() { });
      return requestedBodies(raw, requestedSectionIds);
    } catch (RuntimeException | IOException exception) {
      throw new AgentExecutionException(agentLabel,
          "Unable to parse the section Markdown JSON response.", exception);
    }
  }

  private static Map<String, String> requestedBodies(Map<String, String> raw,
      Set<String> requestedSectionIds) {
    Map<String, String> result = new LinkedHashMap<>();
    raw.forEach((sectionId, markdown) -> {
      if (requestedSectionIds.contains(sectionId) && StringUtils.isNotBlank(markdown)) {
        result.put(sectionId, markdown.trim());
      }
    });
    return Map.copyOf(result);
  }
}
