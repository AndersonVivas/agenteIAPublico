package com.pichincha.cdemsaspopenaidocumentation.service.detector;

import com.pichincha.cdemsaspopenaidocumentation.domain.SignalProfile;
import com.pichincha.cdemsaspopenaidocumentation.domain.SourceFile;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(5)
public class DependencySignalDetector extends LinePatternSignalDetector {

  private static final Set<String> EXTENSIONS = Set.of("java", "kt", "groovy", "scala", "js",
      "jsx", "ts", "tsx", "py", "go", "cs", "php", "rb", "yaml", "yml", "properties", "json",
      "xml");
  private static final List<String> COMMENT_PREFIXES = List.of("//", "*", "/*", "<!--", "#");
  private static final Pattern DEPENDENCY_PATTERN = Pattern.compile(
      "https?://|@FeignClient\\b|\\bHttpExchange\\b|\\bgrpc\\b|\\bkafka\\b|"
          + "\\brabbit(?:mq)?\\b|\\bsqs\\b|\\bsns\\b|\\bredis\\b|"
          + "\\bpostgres(?:ql)?\\b|\\bmysql\\b|\\boracle\\b|\\bmongo(?:db)?\\b|"
          + "\\belasticsearch\\b|\\bazure\\b|\\baws\\b|\\bgcp\\b",
      Pattern.CASE_INSENSITIVE);

  public DependencySignalDetector() {
    super("DEPENDENCY", EXTENSIONS, 250);
  }

  @Override
  protected boolean accepts(SourceFile file, String line) {
    String trimmed = line.trim();
    return COMMENT_PREFIXES.stream().noneMatch(trimmed::startsWith)
        && DEPENDENCY_PATTERN.matcher(line).find();
  }

  @Override
  protected String classify(String line) {
    String normalized = normalize(line);
    if (normalized.contains("http://") || normalized.contains("https://")) {
      return "DEPENDENCY_REMOTE_URL";
    }
    if (normalized.contains("kafka") || normalized.contains("rabbit")
        || normalized.contains("sqs") || normalized.contains("sns")) {
      return "DEPENDENCY_MESSAGING";
    }
    if (normalized.contains("redis") || normalized.contains("postgres")
        || normalized.contains("mysql") || normalized.contains("mongo")) {
      return "DEPENDENCY_DATASTORE";
    }
    return "DEPENDENCY_GENERIC";
  }

  @Override
  public SignalProfile profile(String matchedRule) {
    if ("DEPENDENCY_REMOTE_URL".equals(matchedRule)) {
      return SignalProfile.of("EXTERNAL_DEPENDENCY_REFERENCE", "medium", "runtime", 0.95)
          .withGuidance("External endpoint, DNS, certificate, or network reachability issue.",
              "Validate endpoint health, DNS resolution, and outbound network routes.");
    }
    return SignalProfile.of("EXTERNAL_DEPENDENCY_REFERENCE", "medium", "runtime", 0.88);
  }
}
