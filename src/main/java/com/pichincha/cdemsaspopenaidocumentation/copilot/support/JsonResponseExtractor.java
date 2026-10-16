package com.pichincha.cdemsaspopenaidocumentation.copilot.support;

import org.apache.commons.lang3.StringUtils;

/**
 * Extracts a JSON object from a raw LLM response that may be wrapped in Markdown
 * code fences or preceded/followed by narration text.
 */
public final class JsonResponseExtractor {

  private static final String CODE_FENCE = "(?s)```[a-zA-Z]*";

  private JsonResponseExtractor() {
  }

  public static String extractObject(String rawResponse) {
    String text = StringUtils.defaultString(rawResponse).replaceAll(CODE_FENCE, "").trim();
    int start = text.indexOf('{');
    int end = text.lastIndexOf('}');
    if (start < 0 || end < start) {
      throw new IllegalStateException("Response did not contain a JSON object.");
    }
    return text.substring(start, end + 1);
  }
}
