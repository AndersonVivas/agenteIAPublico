package com.pichincha.cdemsaspopenaidocumentation.copilot.support;

import org.apache.commons.lang3.StringUtils;

/**
 * Extracts a JSON object or array from a raw LLM response that may be wrapped in Markdown
 * code fences or preceded/followed by narration text.
 */
public final class JsonResponseExtractor {

  private JsonResponseExtractor() {
  }

  public static String extractObject(String rawResponse) {
    return extract(rawResponse, '{', '}');
  }

  public static String extractArray(String rawResponse) {
    return extract(rawResponse, '[', ']');
  }

  private static String extract(String rawResponse, char open, char close) {
    String text = StringUtils.defaultString(rawResponse).replaceAll("(?s)```[a-zA-Z]*", "").trim();
    int start = text.indexOf(open);
    int end = text.lastIndexOf(close);
    if (start < 0 || end < start) {
      throw new IllegalStateException("Response did not contain a JSON "
          + (open == '{' ? "object" : "array") + ".");
    }
    return text.substring(start, end + 1);
  }
}
