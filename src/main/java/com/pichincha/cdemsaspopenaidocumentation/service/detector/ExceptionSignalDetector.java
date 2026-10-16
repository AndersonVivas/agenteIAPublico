package com.pichincha.cdemsaspopenaidocumentation.service.detector;

import com.pichincha.cdemsaspopenaidocumentation.domain.SignalProfile;
import com.pichincha.cdemsaspopenaidocumentation.domain.SourceFile;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(3)
public class ExceptionSignalDetector extends LinePatternSignalDetector {

  private static final Set<String> EXTENSIONS = Set.of("java", "kt", "groovy", "scala", "js",
      "jsx", "ts", "tsx", "py", "go", "cs", "php", "rb");
  private static final Pattern EXCEPTION_PATTERN = Pattern.compile(
      "@ExceptionHandler\\b|@(?:Rest)?ControllerAdvice\\b|"
          + "extends\\s+\\w*(?:Runtime)?Exception\\b|"
          + "throw\\s+new\\s+\\w+Exception\\b|"
          + "catch\\s*\\([^)]*Exception[^)]*\\)",
      Pattern.CASE_INSENSITIVE);

  public ExceptionSignalDetector() {
    super("EXCEPTION", EXTENSIONS, 250);
  }

  @Override
  protected boolean accepts(SourceFile file, String line) {
    return EXCEPTION_PATTERN.matcher(line).find();
  }

  @Override
  protected String classify(String line) {
    String normalized = normalize(line);
    if (normalized.contains("@exceptionhandler") || normalized.contains("advice")) {
      return "EXCEPTION_HANDLER";
    }
    if (normalized.contains("throw new")) {
      return "EXCEPTION_THROW_SITE";
    }
    if (normalized.contains("catch") && normalized.contains("exception")) {
      return "EXCEPTION_CATCH_SITE";
    }
    if (normalized.contains("extends") && normalized.contains("exception")) {
      return "EXCEPTION_TYPE";
    }
    return "EXCEPTION_GENERIC";
  }

  @Override
  public SignalProfile profile(String matchedRule) {
    return switch (matchedRule) {
      case "EXCEPTION_HANDLER" -> SignalProfile.of("EXCEPTION_MAPPING_POINT", "medium",
          "request-runtime", 0.98).withGuidance(
          "Custom error handling path may be masking root business failures.",
          "Verify handler mapping, preserve root cause, and enrich structured logs.");
      case "EXCEPTION_THROW_SITE" -> SignalProfile.of("EXCEPTION_THROW_POINT", "medium",
          "request-runtime", 0.96).withGuidance(
          "Unexpected or non-validated input reached domain logic.",
          "Add input validation at boundary and map to controlled 4xx response.");
      case "EXCEPTION_CATCH_SITE" -> SignalProfile.of("EXCEPTION_CATCH_POINT", "medium",
          "request-runtime", 0.88);
      default -> SignalProfile.of("EXCEPTION_TYPE_DECLARATION", "medium", "request-runtime",
          0.88);
    };
  }
}
