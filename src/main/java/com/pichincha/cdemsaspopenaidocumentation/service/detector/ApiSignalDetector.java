package com.pichincha.cdemsaspopenaidocumentation.service.detector;

import com.pichincha.cdemsaspopenaidocumentation.domain.SignalProfile;
import com.pichincha.cdemsaspopenaidocumentation.domain.SourceFile;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class ApiSignalDetector extends LinePatternSignalDetector {

  private static final Set<String> EXTENSIONS = Set.of("java", "kt", "groovy", "scala", "js",
      "jsx", "ts", "tsx", "py", "go", "cs", "php", "rb", "yaml", "yml");
  private static final Pattern API_PATTERN = Pattern.compile(
      "@(?:Request|Get|Post|Put|Delete|Patch)Mapping\\(|@Path\\(|"
          + "@(?:FeignClient|HttpExchange|GetExchange|PostExchange|PutExchange|"
          + "DeleteExchange|PatchExchange)\\b|"
          + "\\b(?:app|router)\\.(?:get|post|put|delete|patch)\\(|"
          + "\\b(?:GET|POST|PUT|DELETE|PATCH)\\s+/[\\w\\-./{}]+",
      Pattern.CASE_INSENSITIVE);
  private static final Pattern OPENAPI_PATH_PATTERN = Pattern.compile(
      "^\\s{2,}/[\\w\\-./{}]+:\\s*$");
  private static final Pattern HTTP_VERB_ROUTE = Pattern.compile(
      ".*\\b(get|post|put|patch|delete)\\s+/[\\w\\-./{}]+.*");

  public ApiSignalDetector() {
    super("API", EXTENSIONS, 250);
  }

  @Override
  protected boolean accepts(SourceFile file, String line) {
    return API_PATTERN.matcher(line).find()
        || isOpenApiFile(file) && OPENAPI_PATH_PATTERN.matcher(line).find();
  }

  @Override
  protected String classify(String line) {
    String normalized = normalize(line);
    if (OPENAPI_PATH_PATTERN.matcher(line).find()) {
      return "API_OPENAPI_PATH";
    }
    if (normalized.contains("@feignclient")) {
      return "API_FEIGN_CLIENT";
    }
    if (normalized.contains("exchange")) {
      return "API_HTTP_EXCHANGE_CLIENT";
    }
    if (normalized.contains("mapping")) {
      return "API_SPRING_ENDPOINT";
    }
    if (normalized.contains("router.") || normalized.contains("app.")) {
      return "API_NODE_ROUTER";
    }
    return HTTP_VERB_ROUTE.matcher(normalized).matches() ? "API_HTTP_VERB_ROUTE" : "API_GENERIC";
  }

  @Override
  public SignalProfile profile(String matchedRule) {
    return switch (matchedRule) {
      case "API_FEIGN_CLIENT" -> SignalProfile.of("REMOTE_CALL_DECLARATION", "high", "request",
          0.88).withGuidance("Remote dependency contract or availability issue.",
          "Validate downstream endpoint, timeout/retry policy, and auth token flow.");
      case "API_HTTP_EXCHANGE_CLIENT" -> SignalProfile.of("REMOTE_CALL_DECLARATION", "low",
          "request", 0.88);
      case "API_SPRING_ENDPOINT" -> SignalProfile.of("HTTP_ENTRYPOINT_DECLARATION", "low",
          "request", 0.99);
      case "API_OPENAPI_PATH" -> SignalProfile.of("HTTP_ENTRYPOINT_DECLARATION", "low",
          "request", 0.94);
      default -> SignalProfile.of("HTTP_ENTRYPOINT_DECLARATION", "low", "request", 0.88);
    };
  }

  private boolean isOpenApiFile(SourceFile file) {
    String name = file.fileName().toLowerCase(Locale.ROOT);
    return name.contains("openapi") || name.contains("swagger");
  }
}
