package com.pichincha.cdemsaspopenaidocumentation.copilot.impl;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;

/**
 * Splits a previously generated Markdown document into a title-to-body map so unchanged
 * sections can be carried forward verbatim into the next, incrementally updated document.
 */
public class MarkdownSectionParser {

  private static final Pattern HEADING_PATTERN = Pattern.compile("(?m)^##\\s+(.+)$");

  public Map<String, String> parseByTitle(String previousMarkdown) {
    Map<String, String> sections = new LinkedHashMap<>();
    if (StringUtils.isBlank(previousMarkdown)) {
      return sections;
    }
    Matcher matcher = HEADING_PATTERN.matcher(previousMarkdown);
    int[] startPositions = matcher.results().mapToInt(result -> result.start()).toArray();
    matcher.reset();
    int headingIndex = 0;
    while (matcher.find()) {
      String title = matcher.group(1).trim();
      int bodyStart = matcher.end();
      int bodyEnd = headingIndex + 1 < startPositions.length
          ? startPositions[headingIndex + 1] : previousMarkdown.length();
      String body = previousMarkdown.substring(bodyStart, bodyEnd).trim();
      sections.put(title, body);
      headingIndex++;
    }
    return sections;
  }
}
