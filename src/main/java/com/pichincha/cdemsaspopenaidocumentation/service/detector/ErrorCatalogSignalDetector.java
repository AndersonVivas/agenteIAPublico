package com.pichincha.cdemsaspopenaidocumentation.service.detector;

import com.pichincha.cdemsaspopenaidocumentation.domain.SignalProfile;
import com.pichincha.cdemsaspopenaidocumentation.domain.SourceFile;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Captures error codes and messages defined in error catalogs (enums of error codes and
 * message property files), which tell what the caller or the end user actually sees.
 */
@Component
@Order(7)
public class ErrorCatalogSignalDetector extends LinePatternSignalDetector {

  private static final String RULE = "EXCEPTION_MESSAGE_CATALOG";
  private static final String PROPERTIES = "properties";
  private static final Pattern CODE_CATALOG_FILE = Pattern.compile(
      "(?i).*(error|exception|message|code|catalog).*");
  private static final Pattern MESSAGE_FILE = Pattern.compile("(?i).*(message|error).*");
  private static final Pattern ENUM_MESSAGE = Pattern.compile(
      "^\\s*[A-Z][A-Z0-9_]{2,}\\s*\\(.*\"[^\"]{6,}\"");
  private static final Pattern PROPERTY_MESSAGE = Pattern.compile(
      "^\\s*[\\w.\\-]+\\s*[=:]\\s*\\S.{5,}");

  public ErrorCatalogSignalDetector() {
    super("EXCEPTION", Set.of("java", "kt", PROPERTIES), 250);
  }

  @Override
  protected boolean supportsFile(SourceFile file) {
    Pattern namePattern = PROPERTIES.equals(file.extension()) ? MESSAGE_FILE : CODE_CATALOG_FILE;
    return namePattern.matcher(file.fileName()).matches();
  }

  @Override
  protected boolean accepts(SourceFile file, String line) {
    if (PROPERTIES.equals(file.extension())) {
      String trimmed = line.trim();
      return !trimmed.startsWith("#") && !trimmed.startsWith("!")
          && PROPERTY_MESSAGE.matcher(line).find();
    }
    return ENUM_MESSAGE.matcher(line).find();
  }

  @Override
  protected String classify(String line) {
    return RULE;
  }

  @Override
  public SignalProfile profile(String matchedRule) {
    return SignalProfile.of("EXCEPTION_MESSAGE_DEFINITION", "medium", "request-runtime", 0.9)
        .withGuidance("Error code or message that callers or end users receive.",
            "Use the message to explain the error and the expected corrective action.");
  }
}
