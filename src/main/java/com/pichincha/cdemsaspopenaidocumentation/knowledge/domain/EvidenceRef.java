package com.pichincha.cdemsaspopenaidocumentation.knowledge.domain;

import org.apache.commons.lang3.StringUtils;

public record EvidenceRef(
    String evidenceId,
    String filePath,
    String ruleId,
    String signalType,
    double confidence) {

  public EvidenceRef {
    evidenceId = normalize(evidenceId);
    filePath = normalize(filePath);
    ruleId = normalize(ruleId);
    signalType = normalize(signalType);
  }

  private static String normalize(String value) {
    return StringUtils.defaultString(value).trim();
  }
}
