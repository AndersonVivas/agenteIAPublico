package com.pichincha.cdemsaspopenaidocumentation.domain;

public record SignalProfile(
    String ruleId,
    String severityHint,
    String runtimeSurface,
    double confidence,
    String probableCause,
    String recoveryAction) {

  private static final String DEFAULT_PROBABLE_CAUSE =
      "Pattern indicates a relevant execution risk that needs contextual triage.";
  private static final String DEFAULT_RECOVERY_ACTION =
      "Review stack trace and surrounding context to define targeted mitigation.";

  public static SignalProfile of(String ruleId, String severityHint, String runtimeSurface,
      double confidence) {
    return new SignalProfile(ruleId, severityHint, runtimeSurface, confidence,
        DEFAULT_PROBABLE_CAUSE, DEFAULT_RECOVERY_ACTION);
  }

  public SignalProfile withGuidance(String cause, String action) {
    return new SignalProfile(ruleId, severityHint, runtimeSurface, confidence, cause, action);
  }
}
