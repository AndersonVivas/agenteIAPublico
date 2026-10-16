package com.pichincha.cdemsaspopenaidocumentation.copilot.support;

import java.util.List;
import java.util.stream.Collectors;

public final class GlobalRulesPromptFormatter {

  private static final String NO_RULES = "- Sin reglas adicionales.";

  private GlobalRulesPromptFormatter() {
  }

  public static String format(List<String> rules) {
    if (rules.isEmpty()) {
      return NO_RULES;
    }
    return rules.stream().map(rule -> "- " + rule).collect(Collectors.joining("\n"));
  }
}
