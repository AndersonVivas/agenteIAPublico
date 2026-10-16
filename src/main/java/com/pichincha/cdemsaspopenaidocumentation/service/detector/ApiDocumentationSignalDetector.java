package com.pichincha.cdemsaspopenaidocumentation.service.detector;

import com.pichincha.cdemsaspopenaidocumentation.domain.SignalProfile;
import com.pichincha.cdemsaspopenaidocumentation.domain.SourceFile;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Captures the business descriptions written in OpenAPI contracts (titles, summaries,
 * descriptions and operation ids), which explain in plain language what each operation does.
 */
@Component
@Order(6)
public class ApiDocumentationSignalDetector extends LinePatternSignalDetector {

  private static final String RULE = "API_OPENAPI_DOCUMENTATION";
  private static final int MIN_LINE_LENGTH = 12;
  private static final Pattern DOCUMENTATION_PATTERN = Pattern.compile(
      "^\\s*\"?(title|summary|description|operationId)\"?\\s*:\\s*(?![|>][-+]?\\s*$)\\S",
      Pattern.CASE_INSENSITIVE);

  public ApiDocumentationSignalDetector() {
    super("API", Set.of("yaml", "yml", "json"), 250);
  }

  @Override
  protected boolean supportsFile(SourceFile file) {
    String name = file.fileName().toLowerCase(Locale.ROOT);
    return name.contains("openapi") || name.contains("swagger");
  }

  @Override
  protected boolean accepts(SourceFile file, String line) {
    return line.trim().length() >= MIN_LINE_LENGTH
        && DOCUMENTATION_PATTERN.matcher(line).find();
  }

  @Override
  protected String classify(String line) {
    return RULE;
  }

  @Override
  public SignalProfile profile(String matchedRule) {
    return SignalProfile.of("API_BUSINESS_DESCRIPTION", "low", "request", 0.9).withGuidance(
        "Business description of an exposed API operation, field or contract.",
        "Use this description to explain the operation in business terms.");
  }
}
