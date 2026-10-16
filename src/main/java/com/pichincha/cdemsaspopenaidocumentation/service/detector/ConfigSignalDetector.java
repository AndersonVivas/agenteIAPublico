package com.pichincha.cdemsaspopenaidocumentation.service.detector;

import com.pichincha.cdemsaspopenaidocumentation.domain.SignalProfile;
import com.pichincha.cdemsaspopenaidocumentation.domain.SourceFile;
import com.pichincha.cdemsaspopenaidocumentation.helper.FileTypeResolver;
import java.util.regex.Pattern;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(4)
public class ConfigSignalDetector extends LinePatternSignalDetector {

  private static final int MIN_LINE_LENGTH = 8;
  private static final Pattern CONFIG_PATTERN = Pattern.compile(
      "\\$\\{[^}]+}|"
          + "\\bspring\\.profiles\\b|"
          + "\\btimeout\\b|"
          + "\\bretry\\w*\\b|"
          + "\\bpool\\b|"
          + "\\bfeature[-_.]?flag\\b|"
          + "\\bcircuit[-_.]?breaker\\b",
      Pattern.CASE_INSENSITIVE);

  public ConfigSignalDetector() {
    super("CONFIG", FileTypeResolver.CONFIG_EXTENSIONS, 250);
  }

  @Override
  protected boolean accepts(SourceFile file, String line) {
    return line.trim().length() >= MIN_LINE_LENGTH && CONFIG_PATTERN.matcher(line).find();
  }

  @Override
  protected String classify(String line) {
    String normalized = normalize(line);
    if (normalized.contains("timeout")) {
      return "CONFIG_TIMEOUT";
    }
    if (normalized.contains("retry")) {
      return "CONFIG_RETRY";
    }
    if (normalized.contains("spring.profiles")) {
      return "CONFIG_PROFILE";
    }
    return normalized.contains("${") ? "CONFIG_ENV_REFERENCE" : "CONFIG_GENERIC";
  }

  @Override
  public SignalProfile profile(String matchedRule) {
    if ("CONFIG_TIMEOUT".equals(matchedRule)) {
      return SignalProfile.of("CONFIG_RUNTIME_CONTROL", "medium", "startup", 0.94).withGuidance(
          "Timeout value may be missing, too low, or inconsistent by environment.",
          "Validate timeout settings by environment and align with SLO limits.");
    }
    return SignalProfile.of("CONFIG_RUNTIME_CONTROL", "medium", "startup", 0.88);
  }
}
