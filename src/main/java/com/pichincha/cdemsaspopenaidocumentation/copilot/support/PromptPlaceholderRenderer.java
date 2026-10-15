package com.pichincha.cdemsaspopenaidocumentation.copilot.support;

import java.util.Map;
import org.apache.commons.lang3.StringUtils;

/**
 * Minimal, dependency-free placeholder substitution for externally stored prompt
 * templates. Placeholders use the {@code {{name}}} syntax.
 */
public final class PromptPlaceholderRenderer {

  private PromptPlaceholderRenderer() {
  }

  public static String render(String template, Map<String, String> values) {
    String rendered = StringUtils.defaultString(template);
    for (Map.Entry<String, String> entry : values.entrySet()) {
      rendered = rendered.replace("{{" + entry.getKey() + "}}",
          StringUtils.defaultString(entry.getValue()));
    }
    return rendered;
  }
}
