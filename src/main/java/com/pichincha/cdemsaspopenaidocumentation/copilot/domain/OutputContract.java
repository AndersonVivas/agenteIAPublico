package com.pichincha.cdemsaspopenaidocumentation.copilot.domain;

import java.util.List;
import org.apache.commons.lang3.StringUtils;

public record OutputContract(
    String outputLanguage,
    String missingDataMessage,
    List<String> rules) {

  public OutputContract {
    outputLanguage = StringUtils.defaultString(outputLanguage).trim();
    missingDataMessage = StringUtils.defaultString(missingDataMessage).trim();
    rules = rules == null ? List.of() : List.copyOf(rules);
  }
}
