package com.pichincha.cdemsaspopenaidocumentation.helper;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * Derives descriptive attributes of an evidence line: the technology involved, the runtime
 * dependency surfaces it touches and searchable tags.
 */
@Component
public class SignalClassifier {

  private static final String DEFAULT_DEPENDENCY_SURFACE = "application";
  private static final String DEFAULT_TECHNOLOGY = "generic";
  private static final List<Map.Entry<String, List<String>>> PLATFORM_KEYWORDS = List.of(
      Map.entry("spring-boot", List.of("springboot", "spring-boot")),
      Map.entry("spring", List.of("@feignclient", "spring")),
      Map.entry("azure", List.of("azure")),
      Map.entry("aws", List.of("aws", "amazon")),
      Map.entry("kubernetes", List.of("k8s", "kubernetes")));
  private static final List<Map.Entry<String, List<String>>> COMPONENT_KEYWORDS = List.of(
      Map.entry("node", List.of("router.", "app.")),
      Map.entry("grpc", List.of("grpc")),
      Map.entry("kafka", List.of("kafka")),
      Map.entry("redis", List.of("redis")));
  private static final List<Map.Entry<String, List<String>>> DEPENDENCY_SURFACE_KEYWORDS =
      List.of(
          Map.entry("database", List.of("jdbc", "datasource", "postgres", "mysql")),
          Map.entry("http", List.of("http", "feign", "exchange", "url")),
          Map.entry("queue", List.of("kafka", "rabbit", "sqs", "sns")),
          Map.entry("cache", List.of("redis")),
          Map.entry("auth", List.of("token", "oauth", "jwt", "auth")));
  private static final List<Map.Entry<String, List<String>>> LINE_TAG_KEYWORDS = List.of(
      Map.entry("spring-endpoint", List.of("@getmapping", "@postmapping")),
      Map.entry("api-client", List.of("feignclient", "exchange")),
      Map.entry("db-connection", List.of("jdbc:", "datasource")),
      Map.entry("persistence-model", List.of("jparepository", "@entity")),
      Map.entry("error-handler", List.of("@exceptionhandler", "advice")),
      Map.entry("throw-site", List.of("throw new")),
      Map.entry("timeout", List.of("timeout")),
      Map.entry("retry", List.of("retry")),
      Map.entry("remote-url", List.of("http://", "https://")));

  public String technology(String line, String filePath) {
    String normalizedLine = normalize(line);
    String normalizedPath = normalize(filePath);
    List<String> platforms = matchingKeys(PLATFORM_KEYWORDS, normalizedLine);
    if (!platforms.isEmpty()) {
      return platforms.getFirst();
    }
    if (normalizedPath.contains("k8s")) {
      return "kubernetes";
    }
    List<String> components = matchingKeys(COMPONENT_KEYWORDS, normalizedLine);
    if (!components.isEmpty()) {
      return components.getFirst();
    }
    return normalizedPath.contains("docker") ? "docker" : DEFAULT_TECHNOLOGY;
  }

  public List<String> dependencySurface(String line) {
    List<String> surfaces = matchingKeys(DEPENDENCY_SURFACE_KEYWORDS, normalize(line));
    return surfaces.isEmpty() ? List.of(DEFAULT_DEPENDENCY_SURFACE) : surfaces;
  }

  public List<String> tags(String signalType, String line, String matchedRule) {
    Set<String> tags = new LinkedHashSet<>(List.of(normalize(signalType),
        normalize(matchedRule)));
    tags.addAll(matchingKeys(LINE_TAG_KEYWORDS, normalize(line)));
    return List.copyOf(tags);
  }

  private List<String> matchingKeys(List<Map.Entry<String, List<String>>> keywordsByKey,
      String normalizedText) {
    return keywordsByKey.stream()
        .filter(entry -> entry.getValue().stream().anyMatch(normalizedText::contains))
        .map(Map.Entry::getKey)
        .toList();
  }

  private String normalize(String value) {
    return value.toLowerCase(Locale.ROOT);
  }
}
