package com.pichincha.cdemsaspopenaidocumentation.service.impl;

import com.pichincha.cdemsaspopenaidocumentation.domain.CodeSignalEvidence;
import com.pichincha.cdemsaspopenaidocumentation.domain.CodeScanSummary;
import com.pichincha.cdemsaspopenaidocumentation.domain.ScanExecutionMetadata;
import com.pichincha.cdemsaspopenaidocumentation.exception.RepositoryCloneException;
import com.pichincha.cdemsaspopenaidocumentation.service.CodeScanService;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;

@Component
public class RepositoryCodeScanService implements CodeScanService {

  private static final Set<String> IGNORED_DIRECTORIES = Set.of(".git", "build",
      "target", "node_modules", "dist", "out", ".gradle", ".idea");
  private static final Set<String> API_EXTENSIONS = Set.of("java", "kt", "groovy",
      "scala", "js", "jsx", "ts", "tsx", "py", "go", "cs", "php", "rb", "yaml",
      "yml");
  private static final Set<String> DB_EXTENSIONS = Set.of("java", "kt", "groovy", "scala",
      "properties", "yaml", "yml", "sql", "xml", "json");
  private static final Set<String> EXCEPTION_EXTENSIONS = Set.of("java", "kt", "groovy",
      "scala", "js", "jsx", "ts", "tsx", "py", "go", "cs", "php", "rb");
    private static final Set<String> CONFIG_EXTENSIONS = Set.of("yaml", "yml",
      "properties", "json", "xml", "toml", "ini", "env", "conf");
    private static final Set<String> DEPENDENCY_EXTENSIONS = Set.of("java", "kt",
      "groovy", "scala", "js", "jsx", "ts", "tsx", "py", "go", "cs",
      "php", "rb", "yaml", "yml", "properties", "json", "xml");
  private static final long MAX_READABLE_FILE_BYTES = 2_000_000;
  private static final int SNIPPET_CONTEXT_LINES = 3;
  private static final int SYMBOL_SEARCH_WINDOW = 30;
  private static final int MAX_API_INDICATORS = 250;
  private static final int MAX_DB_INDICATORS = 200;
  private static final int MAX_EXCEPTION_INDICATORS = 250;
  private static final int MAX_CONFIG_INDICATORS = 250;
  private static final int MAX_DEPENDENCY_INDICATORS = 250;
  private static final String ANALYZER_VERSION = "2.2.0";
  private static final Map<String, String> LANGUAGE_BY_EXTENSION = Map.ofEntries(
      Map.entry("java", "java"),
      Map.entry("kt", "kotlin"),
      Map.entry("groovy", "groovy"),
      Map.entry("scala", "scala"),
      Map.entry("js", "javascript"),
      Map.entry("jsx", "javascript"),
      Map.entry("ts", "typescript"),
      Map.entry("tsx", "typescript"),
      Map.entry("py", "python"),
      Map.entry("go", "go"),
      Map.entry("cs", "csharp"),
      Map.entry("rb", "ruby"),
      Map.entry("php", "php"),
      Map.entry("rs", "rust"),
      Map.entry("sql", "sql"),
      Map.entry("xml", "xml"),
      Map.entry("yml", "yaml"),
      Map.entry("yaml", "yaml"),
      Map.entry("json", "json"),
      Map.entry("properties", "properties"),
      Map.entry("md", "markdown"),
      Map.entry("sh", "shell"),
      Map.entry("bash", "shell"));
  private static final Set<String> TEXT_EXTENSIONS = Set.of("java", "kt", "groovy",
      "scala", "js", "jsx", "ts", "tsx", "py", "go", "cs", "php", "rb", "rs",
      "sql", "xml", "yaml", "yml", "json", "properties", "md", "sh", "bash");
  private static final Pattern API_PATTERN = Pattern.compile(
      "@(?:Request|Get|Post|Put|Delete|Patch)Mapping\\(|@Path\\(|"
          + "@(?:FeignClient|HttpExchange|GetExchange|PostExchange|PutExchange|"
          + "DeleteExchange|PatchExchange)\\b|"
          + "\\b(?:app|router)\\.(?:get|post|put|delete|patch)\\(|"
          + "\\b(?:GET|POST|PUT|DELETE|PATCH)\\s+/[\\w\\-./{}]+",
      Pattern.CASE_INSENSITIVE);
  private static final Pattern OPENAPI_PATH_PATTERN = Pattern.compile(
      "^\\s{2,}/[\\w\\-./{}]+:\\s*$");
  private static final Pattern DB_PATTERN = Pattern.compile(
      "\\bspring\\.datasource\\b|\\bjdbc:[^\\s]+|"
          + "\\bEntityManager\\b|\\bJpaRepository\\b|\\bCrudRepository\\b|"
          + "\\bMongoRepository\\b|\\bRedisTemplate\\b|\\bCassandraTemplate\\b|"
          + "\\bR2dbc\\b|\\bFlyway\\b|\\bLiquibase\\b|"
          + "@Entity\\b|"
          + "\\bSELECT\\b\\s+.*\\bFROM\\b|"
          + "\\bINSERT\\b\\s+\\bINTO\\b|"
          + "\\bUPDATE\\b\\s+\\w+\\s+\\bSET\\b|"
          + "\\bDELETE\\b\\s+\\bFROM\\b",
      Pattern.CASE_INSENSITIVE);
  private static final Pattern EXCEPTION_PATTERN = Pattern.compile(
      "@ExceptionHandler\\b|@(?:Rest)?ControllerAdvice\\b|"
          + "extends\\s+\\w*(?:Runtime)?Exception\\b|"
          + "throw\\s+new\\s+\\w+Exception\\b|"
          + "catch\\s*\\([^)]*Exception[^)]*\\)",
      Pattern.CASE_INSENSITIVE);
    private static final Pattern CONFIG_PATTERN = Pattern.compile(
      "\\$\\{[^}]+}|"
        + "\\bspring\\.profiles\\b|"
        + "\\btimeout\\b|"
        + "\\bretry\\w*\\b|"
        + "\\bpool\\b|"
        + "\\bfeature[-_.]?flag\\b|"
        + "\\bcircuit[-_.]?breaker\\b",
      Pattern.CASE_INSENSITIVE);
    private static final Pattern DEPENDENCY_PATTERN = Pattern.compile(
      "https?://|"
        + "@FeignClient\\b|"
        + "\\bHttpExchange\\b|"
        + "\\bgrpc\\b|"
        + "\\bkafka\\b|"
        + "\\brabbit(?:mq)?\\b|"
        + "\\bsqs\\b|"
        + "\\bsns\\b|"
        + "\\bredis\\b|"
        + "\\bpostgres(?:ql)?\\b|"
        + "\\bmysql\\b|"
        + "\\boracle\\b|"
        + "\\bmongo(?:db)?\\b|"
        + "\\belasticsearch\\b|"
        + "\\bazure\\b|"
        + "\\baws\\b|"
        + "\\bgcp\\b",
      Pattern.CASE_INSENSITIVE);

  @Override
  public CodeScanSummary scanRepository(String localRepositoryPath) {
    long startedAt = System.currentTimeMillis();
    Path root = Path.of(localRepositoryPath);
    validatePath(root);
    List<Path> allRegularFiles = listAllRegularFiles(root);
    List<Path> regularFiles = filterScannableFiles(root, allRegularFiles);
    List<CodeSignalEvidence> apiEvidence = collectEvidence(root, regularFiles,
      API_PATTERN, API_EXTENSIONS, "API", MAX_API_INDICATORS, true);
    List<CodeSignalEvidence> dbEvidence = collectEvidence(root, regularFiles,
      DB_PATTERN, DB_EXTENSIONS, "DATABASE", MAX_DB_INDICATORS, false);
    List<CodeSignalEvidence> exceptionEvidence = collectEvidence(root, regularFiles,
      EXCEPTION_PATTERN, EXCEPTION_EXTENSIONS, "EXCEPTION",
      MAX_EXCEPTION_INDICATORS, false);
    List<CodeSignalEvidence> configEvidence = collectEvidence(root, regularFiles,
      CONFIG_PATTERN, CONFIG_EXTENSIONS, "CONFIG", MAX_CONFIG_INDICATORS, false);
    List<CodeSignalEvidence> dependencyEvidence = collectEvidence(root, regularFiles,
      DEPENDENCY_PATTERN, DEPENDENCY_EXTENSIONS, "DEPENDENCY",
      MAX_DEPENDENCY_INDICATORS, false);
    List<CodeSignalEvidence> evidences = mergeEvidence(apiEvidence, dbEvidence,
      exceptionEvidence, configEvidence, dependencyEvidence);
    long scannedBytes = countScannedBytes(regularFiles);
    return new CodeScanSummary(
        regularFiles.size(),
        countByExtension(regularFiles, "java"),
        countByExtension(regularFiles, "yml") + countByExtension(regularFiles, "yaml"),
        countByExtension(regularFiles, "md"),
        countDirectories(root),
        countByLanguage(regularFiles),
        countByExtension(regularFiles),
      toIndicatorStrings(apiEvidence),
      toIndicatorStrings(dbEvidence),
      toIndicatorStrings(exceptionEvidence),
      evidences,
      buildMetadata(root, startedAt, allRegularFiles.size(), regularFiles.size(),
        apiEvidence.size(), dbEvidence.size(), exceptionEvidence.size(),
        configEvidence.size(), dependencyEvidence.size(),
        evidences.size(), scannedBytes));
  }

  private void validatePath(Path root) {
    if (!Files.exists(root) || !Files.isDirectory(root)) {
      throw new RepositoryCloneException("Repository path does not exist: " + root);
    }
  }

  private List<Path> listAllRegularFiles(Path root) {
    try (Stream<Path> walk = Files.walk(root)) {
      return walk.filter(Files::isRegularFile).toList();
    } catch (IOException exception) {
      throw new RepositoryCloneException("Unable to scan repository.", exception);
    }
  }

  private List<Path> filterScannableFiles(Path root, List<Path> files) {
    return files.stream()
        .filter(path -> !isIgnored(root, path))
        .filter(this::isSmallEnough)
        .toList();
  }

  private long countDirectories(Path root) {
    try (Stream<Path> walk = Files.walk(root)) {
      return walk.filter(Files::isDirectory)
          .filter(path -> !isIgnored(root, path))
          .count();
    } catch (IOException exception) {
      throw new RepositoryCloneException("Unable to scan repository.", exception);
    }
  }

  private long countByExtension(List<Path> files, String extension) {
    return files.stream().filter(path -> extension(path).equals(extension)).count();
  }

  private Map<String, Long> countByLanguage(List<Path> files) {
    Map<String, Long> counters = new LinkedHashMap<>();
    for (Path path : files) {
      String language = languageFor(path);
      counters.put(language, counters.getOrDefault(language, 0L) + 1L);
    }
    return counters;
  }

  private Map<String, Long> countByExtension(List<Path> files) {
    Map<String, Long> counters = new LinkedHashMap<>();
    for (Path path : files) {
      String extension = extension(path);
      counters.put(extension, counters.getOrDefault(extension, 0L) + 1L);
    }
    return counters;
  }

  private List<CodeSignalEvidence> collectEvidence(Path root, List<Path> files,
      Pattern pattern, Set<String> allowedExtensions, String signalType,
      int maxIndicators, boolean includeOpenApiPaths) {
    List<CodeSignalEvidence> evidences = new ArrayList<>();
    for (Path path : files) {
      if (!isTextFile(path) || !allowedExtensions.contains(extension(path))) {
        continue;
      }
      collectFileEvidence(root, path, pattern, signalType, includeOpenApiPaths,
          maxIndicators, evidences);
      if (evidences.size() >= maxIndicators) {
        break;
      }
    }
    return List.copyOf(evidences);
  }

  private void collectFileEvidence(Path root, Path path, Pattern pattern,
      String signalType, boolean includeOpenApiPaths, int maxIndicators,
      List<CodeSignalEvidence> evidences) {
    List<String> lines = readLines(path);
    for (int index = 0; index < lines.size(); index++) {
      String line = lines.get(index);
      boolean matched = pattern.matcher(line).find();
      if (!matched && includeOpenApiPaths && isOpenApiPathLine(path, line)) {
        matched = true;
      }
      if (matched) {
        evidences.add(buildEvidence(root, path, lines, index, signalType));
      }
      if (evidences.size() >= maxIndicators) {
        return;
      }
    }
  }

  private CodeSignalEvidence buildEvidence(Path root, Path path, List<String> lines,
      int lineIndex, String signalType) {
    int start = Math.max(1, lineIndex + 1 - SNIPPET_CONTEXT_LINES);
    int end = Math.min(lines.size(), lineIndex + 1 + SNIPPET_CONTEXT_LINES);
    String relativePath = root.relativize(path).toString();
    String matchLine = lines.get(lineIndex).trim();
    String matchedRule = detectRule(signalType, matchLine);
    String symbolName = findSymbolName(path, lines, lineIndex, matchedRule);
    String scopeSymbol = findScopeSymbol(path, lines, lineIndex);
    int matchLineNumber = lineIndex + 1;
    String language = languageFor(path);
    String sourceKind = sourceKindFor(path);
    String technology = technologyFor(signalType, matchLine, path);
    String ruleId = normalizeRuleId(signalType, matchedRule);
    String severityHint = severityHintFor(signalType, matchedRule);
    String runtimeSurface = runtimeSurfaceFor(signalType, matchedRule);
    List<String> dependencySurface = dependencySurfaceFor(matchLine);
    String evidenceId = buildEvidenceId(relativePath, signalType, matchedRule,
      matchLineNumber, matchLine);
    return new CodeSignalEvidence(
        signalType,
      sourceKind,
      language,
      technology,
      relativePath,
        symbolName,
      scopeSymbol,
      evidenceId,
        ruleId,
        matchedRule,
        matchLine,
      severityHint,
      runtimeSurface,
      dependencySurface,
      probableCauseFor(signalType, matchedRule),
      recoveryActionFor(signalType, matchedRule),
        start,
        end,
      matchLineNumber,
        snippetLines(lines, start, end),
        confidenceFor(signalType, matchedRule),
        tagsFor(signalType, matchLine, matchedRule));
  }

  private String sourceKindFor(Path path) {
    String extension = extension(path);
    if (extension.equals("md") || extension.equals("txt")) {
      return "docs";
    }
    if (CONFIG_EXTENSIONS.contains(extension)) {
      return "config";
    }
    if (TEXT_EXTENSIONS.contains(extension)) {
      return "code";
    }
    return "other";
  }

  private String technologyFor(String signalType, String line, Path path) {
    String normalized = line.toLowerCase();
    if (normalized.contains("@feignclient") || normalized.contains("spring")) {
      return "spring";
    }
    if (normalized.contains("router.") || normalized.contains("app.")) {
      return "node";
    }
    if (normalized.contains("grpc")) {
      return "grpc";
    }
    if (normalized.contains("kafka")) {
      return "kafka";
    }
    if (normalized.contains("redis")) {
      return "redis";
    }
    if (signalType.equals("CONFIG")
        && path.getFileName().toString().toLowerCase().contains("docker")) {
      return "container";
    }
    return "generic";
  }

  private String normalizeRuleId(String signalType, String matchedRule) {
    if (signalType.equals("API")) {
      return apiRuleId(matchedRule);
    }
    if (signalType.equals("DATABASE")) {
      return databaseRuleId(matchedRule);
    }
    if (signalType.equals("EXCEPTION")) {
      return exceptionRuleId(matchedRule);
    }
    if (signalType.equals("CONFIG")) {
      return "CONFIG_RUNTIME_CONTROL";
    }
    if (signalType.equals("DEPENDENCY")) {
      return "EXTERNAL_DEPENDENCY_REFERENCE";
    }
    return "GENERIC_RUNTIME_SIGNAL";
  }

  private String apiRuleId(String matchedRule) {
    if (matchedRule.equals("API_FEIGN_CLIENT")
        || matchedRule.equals("API_HTTP_EXCHANGE_CLIENT")) {
      return "REMOTE_CALL_DECLARATION";
    }
    return "HTTP_ENTRYPOINT_DECLARATION";
  }

  private String databaseRuleId(String matchedRule) {
    if (matchedRule.equals("DB_CONNECTION")) {
      return "DATASTORE_CONNECTION_CONFIG";
    }
    if (matchedRule.equals("DB_SQL_STATEMENT")) {
      return "DATASTORE_QUERY_USAGE";
    }
    return "DATASTORE_ACCESS_PATTERN";
  }

  private String exceptionRuleId(String matchedRule) {
    if (matchedRule.equals("EXCEPTION_HANDLER")) {
      return "EXCEPTION_MAPPING_POINT";
    }
    if (matchedRule.equals("EXCEPTION_THROW_SITE")) {
      return "EXCEPTION_THROW_POINT";
    }
    if (matchedRule.equals("EXCEPTION_CATCH_SITE")) {
      return "EXCEPTION_CATCH_POINT";
    }
    return "EXCEPTION_TYPE_DECLARATION";
  }

  private String severityHintFor(String signalType, String matchedRule) {
    if (matchedRule.equals("DB_CONNECTION") || matchedRule.equals("API_FEIGN_CLIENT")) {
      return "high";
    }
    if (signalType.equals("EXCEPTION") || signalType.equals("DEPENDENCY")) {
      return "medium";
    }
    if (signalType.equals("CONFIG")) {
      return "medium";
    }
    return "low";
  }

  private String runtimeSurfaceFor(String signalType, String matchedRule) {
    if (signalType.equals("API")) {
      return "request";
    }
    if (signalType.equals("CONFIG") || matchedRule.equals("DB_CONNECTION")) {
      return "startup";
    }
    if (signalType.equals("DEPENDENCY")) {
      return "runtime";
    }
    if (signalType.equals("EXCEPTION")) {
      return "request-runtime";
    }
    return "runtime";
  }

  private List<String> dependencySurfaceFor(String line) {
    String normalized = line.toLowerCase();
    Set<String> surfaces = new LinkedHashSet<>();
    if (normalized.contains("jdbc") || normalized.contains("datasource")
        || normalized.contains("postgres") || normalized.contains("mysql")) {
      surfaces.add("database");
    }
    if (normalized.contains("http") || normalized.contains("feign")
        || normalized.contains("exchange") || normalized.contains("url")) {
      surfaces.add("http");
    }
    if (normalized.contains("kafka") || normalized.contains("rabbit")
        || normalized.contains("sqs") || normalized.contains("sns")) {
      surfaces.add("queue");
    }
    if (normalized.contains("redis")) {
      surfaces.add("cache");
    }
    if (normalized.contains("token") || normalized.contains("oauth")
        || normalized.contains("jwt") || normalized.contains("auth")) {
      surfaces.add("auth");
    }
    if (surfaces.isEmpty()) {
      surfaces.add("application");
    }
    return List.copyOf(surfaces);
  }

  private String findSymbolName(Path path, List<String> lines, int lineIndex,
      String matchedRule) {
    String sameLineName = extractSymbolFromSameLine(lines.get(lineIndex), matchedRule);
    if (!sameLineName.isBlank()) {
      return sameLineName;
    }
    String backwardName = findBackwardSymbol(lines, lineIndex);
    if (!backwardName.isBlank()) {
      return backwardName;
    }
    String forwardName = findForwardSymbol(lines, lineIndex);
    if (!forwardName.isBlank()) {
      return forwardName;
    }
    return path.getFileName().toString();
  }

  private String extractSymbolFromSameLine(String rawLine, String matchedRule) {
    String line = rawLine.trim();
    if (matchedRule.equals("API_OPENAPI_PATH")) {
      int endIndex = line.endsWith(":") ? line.length() - 1 : line.length();
      return endIndex > 0 ? line.substring(0, endIndex) : "";
    }
    String annotationName = extractAnnotationName(line);
    if (!annotationName.isBlank()) {
      return annotationName;
    }
    String className = extractTypeName(line);
    if (!className.isBlank()) {
      return className;
    }
    return extractMethodName(line);
  }

  private String findBackwardSymbol(List<String> lines, int lineIndex) {
    int lowerBound = Math.max(0, lineIndex - SYMBOL_SEARCH_WINDOW);
    for (int cursor = lineIndex; cursor >= lowerBound; cursor--) {
      String line = lines.get(cursor).trim();
      String className = extractTypeName(line);
      if (!className.isBlank()) {
        return className;
      }
      String methodName = extractMethodName(line);
      if (!methodName.isBlank()) {
        return methodName;
      }
    }
    return "";
  }

  private String findForwardSymbol(List<String> lines, int lineIndex) {
    int upperBound = Math.min(lines.size() - 1, lineIndex + SYMBOL_SEARCH_WINDOW);
    for (int cursor = lineIndex + 1; cursor <= upperBound; cursor++) {
      String line = lines.get(cursor).trim();
      String className = extractTypeName(line);
      if (!className.isBlank()) {
        return className;
      }
      String methodName = extractMethodName(line);
      if (!methodName.isBlank()) {
        return methodName;
      }
    }
    return "";
  }

  private String findScopeSymbol(Path path, List<String> lines, int lineIndex) {
    String backwardName = findBackwardSymbol(lines, lineIndex);
    if (!backwardName.isBlank()) {
      return backwardName;
    }
    String forwardName = findForwardSymbol(lines, lineIndex);
    if (!forwardName.isBlank()) {
      return forwardName;
    }
    return path.getFileName().toString();
  }

  private String extractTypeName(String line) {
    Pattern typePattern = Pattern.compile("\\b(class|interface|enum|record)\\s+([A-Za-z0-9_]+)");
    var matcher = typePattern.matcher(line);
    return matcher.find() ? matcher.group(2) : "";
  }

  private String extractAnnotationName(String line) {
    Pattern annotationPattern = Pattern.compile("@([A-Za-z0-9_]+)");
    var matcher = annotationPattern.matcher(line);
    return matcher.find() ? matcher.group(1) : "";
  }

  private String extractMethodName(String line) {
    Pattern methodPattern = Pattern.compile("([A-Za-z0-9_]+)\\s*\\([^)]*\\)\\s*\\{?");
    if (line.startsWith("if") || line.startsWith("for") || line.startsWith("while")) {
      return "";
    }
    var matcher = methodPattern.matcher(line);
    return matcher.find() ? matcher.group(1) : "";
  }

  private List<String> snippetLines(List<String> lines, int start, int end) {
    List<String> snippet = new ArrayList<>();
    for (int line = start; line <= end; line++) {
      snippet.add(line + ": " + lines.get(line - 1));
    }
    return snippet;
  }

  private String detectRule(String signalType, String line) {
    String normalized = line.toLowerCase();
    if (signalType.equals("API")) {
      return apiRule(normalized);
    }
    if (signalType.equals("DATABASE")) {
      return databaseRule(normalized);
    }
    if (signalType.equals("EXCEPTION")) {
      return exceptionRule(normalized);
    }
    if (signalType.equals("CONFIG")) {
      return configRule(normalized);
    }
    if (signalType.equals("DEPENDENCY")) {
      return dependencyRule(normalized);
    }
    return "UNKNOWN_RULE";
  }

  private String configRule(String line) {
    if (line.contains("timeout")) {
      return "CONFIG_TIMEOUT";
    }
    if (line.contains("retry")) {
      return "CONFIG_RETRY";
    }
    if (line.contains("spring.profiles")) {
      return "CONFIG_PROFILE";
    }
    if (line.contains("${")) {
      return "CONFIG_ENV_REFERENCE";
    }
    return "CONFIG_GENERIC";
  }

  private String dependencyRule(String line) {
    if (line.contains("http://") || line.contains("https://")) {
      return "DEPENDENCY_REMOTE_URL";
    }
    if (line.contains("kafka") || line.contains("rabbit")
        || line.contains("sqs") || line.contains("sns")) {
      return "DEPENDENCY_MESSAGING";
    }
    if (line.contains("redis") || line.contains("postgres")
        || line.contains("mysql") || line.contains("mongo")) {
      return "DEPENDENCY_DATASTORE";
    }
    return "DEPENDENCY_GENERIC";
  }

  private String apiRule(String line) {
    if (OPENAPI_PATH_PATTERN.matcher(line).find()) {
      return "API_OPENAPI_PATH";
    }
    if (line.contains("@feignclient")) {
      return "API_FEIGN_CLIENT";
    }
    if (line.contains("exchange")) {
      return "API_HTTP_EXCHANGE_CLIENT";
    }
    if (line.contains("mapping")) {
      return "API_SPRING_ENDPOINT";
    }
    if (line.contains("router.") || line.contains("app.")) {
      return "API_NODE_ROUTER";
    }
    if (line.matches(".*\\b(get|post|put|patch|delete)\\s+/[\\w\\-./{}]+.*")) {
      return "API_HTTP_VERB_ROUTE";
    }
    return "API_GENERIC";
  }

  private String databaseRule(String line) {
    if (line.contains("jdbc:") || line.contains("datasource")) {
      return "DB_CONNECTION";
    }
    if (line.contains("@entity") || line.contains("jparepository")
        || line.contains("crudrepository") || line.contains("mongorepository")) {
      return "DB_PERSISTENCE_MODEL";
    }
    if (line.contains("select") || line.contains("insert")
        || line.contains("update") || line.contains("delete")) {
      return "DB_SQL_STATEMENT";
    }
    return "DB_GENERIC";
  }

  private String exceptionRule(String line) {
    if (line.contains("@exceptionhandler") || line.contains("advice")) {
      return "EXCEPTION_HANDLER";
    }
    if (line.contains("throw new")) {
      return "EXCEPTION_THROW_SITE";
    }
    if (line.contains("catch") && line.contains("exception")) {
      return "EXCEPTION_CATCH_SITE";
    }
    if (line.contains("extends") && line.contains("exception")) {
      return "EXCEPTION_TYPE";
    }
    return "EXCEPTION_GENERIC";
  }

  private double confidenceFor(String signalType, String matchedRule) {
    if (signalType.equals("API") && matchedRule.equals("API_SPRING_ENDPOINT")) {
      return 0.99;
    }
    if (signalType.equals("API") && matchedRule.equals("API_OPENAPI_PATH")) {
      return 0.94;
    }
    if (signalType.equals("DATABASE") && matchedRule.equals("DB_CONNECTION")) {
      return 0.99;
    }
    if (signalType.equals("EXCEPTION") && matchedRule.equals("EXCEPTION_HANDLER")) {
      return 0.98;
    }
    if (signalType.equals("EXCEPTION") && matchedRule.equals("EXCEPTION_THROW_SITE")) {
      return 0.96;
    }
    if (signalType.equals("CONFIG") && matchedRule.equals("CONFIG_TIMEOUT")) {
      return 0.94;
    }
    if (signalType.equals("DEPENDENCY") && matchedRule.equals("DEPENDENCY_REMOTE_URL")) {
      return 0.95;
    }
    return 0.88;
  }

  private String probableCauseFor(String signalType, String matchedRule) {
    if (signalType.equals("EXCEPTION") && matchedRule.equals("EXCEPTION_THROW_SITE")) {
      return "Unexpected or non-validated input reached domain logic.";
    }
    if (signalType.equals("EXCEPTION") && matchedRule.equals("EXCEPTION_HANDLER")) {
      return "Custom error handling path may be masking root business failures.";
    }
    if (signalType.equals("DATABASE") && matchedRule.equals("DB_CONNECTION")) {
      return "Database connectivity or credential configuration issue.";
    }
    if (signalType.equals("API") && matchedRule.equals("API_FEIGN_CLIENT")) {
      return "Remote dependency contract or availability issue.";
    }
    if (signalType.equals("CONFIG") && matchedRule.equals("CONFIG_TIMEOUT")) {
      return "Timeout value may be missing, too low, or inconsistent by environment.";
    }
    if (signalType.equals("DEPENDENCY") && matchedRule.equals("DEPENDENCY_REMOTE_URL")) {
      return "External endpoint, DNS, certificate, or network reachability issue.";
    }
    return "Pattern indicates a relevant execution risk that needs contextual triage.";
  }

  private String recoveryActionFor(String signalType, String matchedRule) {
    if (signalType.equals("EXCEPTION") && matchedRule.equals("EXCEPTION_THROW_SITE")) {
      return "Add input validation at boundary and map to controlled 4xx response.";
    }
    if (signalType.equals("EXCEPTION") && matchedRule.equals("EXCEPTION_HANDLER")) {
      return "Verify handler mapping, preserve root cause, and enrich structured logs.";
    }
    if (signalType.equals("DATABASE") && matchedRule.equals("DB_CONNECTION")) {
      return "Check datasource secrets, network route, and database health.";
    }
    if (signalType.equals("API") && matchedRule.equals("API_FEIGN_CLIENT")) {
      return "Validate downstream endpoint, timeout/retry policy, and auth token flow.";
    }
    if (signalType.equals("CONFIG") && matchedRule.equals("CONFIG_TIMEOUT")) {
      return "Validate timeout settings by environment and align with SLO limits.";
    }
    if (signalType.equals("DEPENDENCY") && matchedRule.equals("DEPENDENCY_REMOTE_URL")) {
      return "Validate endpoint health, DNS resolution, and outbound network routes.";
    }
    return "Review stack trace and surrounding context to define targeted mitigation.";
  }

  private String buildEvidenceId(String filePath, String signalType, String matchedRule,
      int matchLine, String matchText) {
    String seed = filePath + "|" + signalType + "|" + matchedRule + "|"
        + matchLine + "|" + matchText;
    return "EV-" + shortSha256(seed);
  }

  private String shortSha256(String value) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
      return hex(hash).substring(0, 12);
    } catch (NoSuchAlgorithmException exception) {
      return Integer.toHexString(value.hashCode());
    }
  }

  private String hex(byte[] bytes) {
    StringBuilder builder = new StringBuilder(bytes.length * 2);
    for (byte current : bytes) {
      builder.append(String.format("%02x", current));
    }
    return builder.toString();
  }

  private List<String> tagsFor(String signalType, String line, String matchedRule) {
    Set<String> tags = new LinkedHashSet<>();
    String normalized = line.toLowerCase();
    tags.add(signalType.toLowerCase());
    tags.add(matchedRule.toLowerCase());
    if (normalized.contains("@getmapping") || normalized.contains("@postmapping")) {
      tags.add("spring-endpoint");
    }
    if (normalized.contains("feignclient") || normalized.contains("exchange")) {
      tags.add("api-client");
    }
    if (normalized.contains("jdbc:") || normalized.contains("datasource")) {
      tags.add("db-connection");
    }
    if (normalized.contains("jparepository") || normalized.contains("@entity")) {
      tags.add("persistence-model");
    }
    if (normalized.contains("@exceptionhandler") || normalized.contains("advice")) {
      tags.add("error-handler");
    }
    if (normalized.contains("throw new")) {
      tags.add("throw-site");
    }
    if (normalized.contains("timeout") || matchedRule.equals("CONFIG_TIMEOUT")) {
      tags.add("timeout");
    }
    if (normalized.contains("retry") || matchedRule.equals("CONFIG_RETRY")) {
      tags.add("retry");
    }
    if (normalized.contains("http://") || normalized.contains("https://")) {
      tags.add("remote-url");
    }
    return List.copyOf(tags);
  }

  private List<CodeSignalEvidence> mergeEvidence(List<CodeSignalEvidence> apiEvidence,
      List<CodeSignalEvidence> dbEvidence, List<CodeSignalEvidence> exceptionEvidence,
      List<CodeSignalEvidence> configEvidence,
      List<CodeSignalEvidence> dependencyEvidence) {
    List<CodeSignalEvidence> merged = new ArrayList<>();
    merged.addAll(apiEvidence);
    merged.addAll(dbEvidence);
    merged.addAll(exceptionEvidence);
    merged.addAll(configEvidence);
    merged.addAll(dependencyEvidence);
    return List.copyOf(merged);
  }

  private List<String> toIndicatorStrings(List<CodeSignalEvidence> evidences) {
    return evidences.stream().map(this::indicatorText).toList();
  }

  private String indicatorText(CodeSignalEvidence evidence) {
    return evidence.filePath()
      + " | id=" + evidence.evidenceId()
        + "#L" + evidence.matchLine()
        + " | lang=" + evidence.language()
        + " | ruleId=" + evidence.ruleId()
        + " | rule=" + evidence.matchedRule()
      + " | scope=" + evidence.scopeSymbol()
        + " | symbol=" + evidence.symbolName()
        + " | text=" + evidence.matchText();
  }

  private ScanExecutionMetadata buildMetadata(Path root, long startedAt,
      int totalFiles, int scannedFiles, int apiSignalsExtracted,
      int databaseSignalsExtracted, int exceptionSignalsExtracted,
      int configSignalsExtracted, int dependencySignalsExtracted,
      int signalsExtracted, long scannedBytes) {
    String previousCommit = envValue("CDE_PREVIOUS_COMMIT", "");
    return new ScanExecutionMetadata(
        !previousCommit.isBlank(),
        previousCommit,
        currentCommit(root),
        envValue("CDE_SCAN_MODE", "FULL"),
        envValue("CDE_PROMPT_VERSION", "unknown"),
        ANALYZER_VERSION,
        System.currentTimeMillis() - startedAt,
        scannedFiles,
        Math.max(0, totalFiles - scannedFiles),
        apiSignalsExtracted,
        databaseSignalsExtracted,
        exceptionSignalsExtracted,
        configSignalsExtracted,
        dependencySignalsExtracted,
        signalsExtracted,
        scannedBytes);
  }

  private String currentCommit(Path root) {
    ProcessBuilder processBuilder = new ProcessBuilder("git", "-C",
        root.toString(), "rev-parse", "HEAD");
    processBuilder.redirectErrorStream(true);
    try {
      Process process = processBuilder.start();
      String output = new String(process.getInputStream().readAllBytes(),
          StandardCharsets.UTF_8).trim();
      int exitCode = process.waitFor();
      return exitCode == 0 && !output.isBlank() ? output : "UNKNOWN";
    } catch (IOException exception) {
      return "UNKNOWN";
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      return "UNKNOWN";
    }
  }

  private String envValue(String key, String defaultValue) {
    String value = System.getenv(key);
    return Objects.requireNonNullElse(value, defaultValue);
  }

  private long countScannedBytes(List<Path> files) {
    long total = 0;
    for (Path path : files) {
      try {
        total += Files.size(path);
      } catch (IOException exception) {
        total += 0;
      }
    }
    return total;
  }

  private boolean isOpenApiPathLine(Path path, String line) {
    String name = path.getFileName().toString().toLowerCase();
    if (!(name.contains("openapi") || name.contains("swagger"))) {
      return false;
    }
    return OPENAPI_PATH_PATTERN.matcher(line).find();
  }

  private List<String> readLines(Path path) {
    try {
      return Files.readAllLines(path, StandardCharsets.UTF_8);
    } catch (IOException exception) {
      return List.of();
    }
  }

  private String languageFor(Path path) {
    return LANGUAGE_BY_EXTENSION.getOrDefault(extension(path), "other");
  }

  private boolean isTextFile(Path path) {
    return TEXT_EXTENSIONS.contains(extension(path));
  }

  private boolean isSmallEnough(Path path) {
    try {
      return Files.size(path) <= MAX_READABLE_FILE_BYTES;
    } catch (IOException exception) {
      return false;
    }
  }

  private boolean isIgnored(Path root, Path path) {
    Path relativePath = root.relativize(path);
    for (Path segment : relativePath) {
      if (IGNORED_DIRECTORIES.contains(segment.toString())) {
        return true;
      }
    }
    return false;
  }

  private String extension(Path path) {
    String name = path.getFileName().toString();
    int index = name.lastIndexOf('.');
    if (index < 0 || index == name.length() - 1) {
      return "no_extension";
    }
    return name.substring(index + 1).toLowerCase();
  }
}