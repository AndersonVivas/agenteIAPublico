package com.pichincha.cdemsaspopenaidocumentation.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pichincha.cdemsaspopenaidocumentation.domain.CodeSignalEvidence;
import com.pichincha.cdemsaspopenaidocumentation.helper.EvidenceFactory;
import com.pichincha.cdemsaspopenaidocumentation.helper.FileTypeResolver;
import com.pichincha.cdemsaspopenaidocumentation.helper.SignalClassifier;
import com.pichincha.cdemsaspopenaidocumentation.helper.SourceFileReader;
import com.pichincha.cdemsaspopenaidocumentation.helper.SymbolLocator;
import com.pichincha.cdemsaspopenaidocumentation.service.detector.ApiDocumentationSignalDetector;
import com.pichincha.cdemsaspopenaidocumentation.service.detector.ApiSignalDetector;
import com.pichincha.cdemsaspopenaidocumentation.service.detector.ConfigSignalDetector;
import com.pichincha.cdemsaspopenaidocumentation.service.detector.DatabaseSignalDetector;
import com.pichincha.cdemsaspopenaidocumentation.service.detector.DependencySignalDetector;
import com.pichincha.cdemsaspopenaidocumentation.service.detector.ErrorCatalogSignalDetector;
import com.pichincha.cdemsaspopenaidocumentation.service.detector.ExceptionSignalDetector;
import com.pichincha.cdemsaspopenaidocumentation.service.detector.ServiceOverviewSignalDetector;
import com.pichincha.cdemsaspopenaidocumentation.service.detector.SignalDetector;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class RepositoryCodeScanServiceTest {

  private final RepositoryCodeScanService service = scanService();

  @TempDir
  private Path repository;

  @Test
  void scanFilesShouldExtractEverySignalTypeWithCompleteEvidence() throws IOException {
    List<String> files = write(Map.of(
        "src/main/java/com/example/controller/AppController.java",
        "@RequestMapping(\"/api/v1\")\nclass AppController {\n"
            + "  @GetMapping(\"/customers\")\n"
            + "  void method() { throw new IllegalArgumentException(); }\n}",
        "src/main/java/com/example/UserEntity.java", "@Entity class UserEntity {}",
        "src/main/resources/application.yml",
        "spring:\n  datasource:\n    url: ${DB_URL:jdbc:postgresql://localhost:5432/test}\n"
            + "app:\n  timeout: 5000"));

    List<CodeSignalEvidence> evidences = service.scanFiles(repository, files, "scan-1");

    assertTrue(List.of("API", "DATABASE", "EXCEPTION", "CONFIG", "DEPENDENCY").stream()
        .allMatch(type -> evidences.stream().anyMatch(item -> item.signalType().equals(type))));
    assertTrue(evidences.stream().allMatch(item -> item.scanId().equals("scan-1")
        && item.evidenceId().startsWith("EV-") && !item.snippet().isEmpty()
        && !item.ruleId().isBlank() && !item.scopeSymbol().isBlank()
        && !item.dependencySurface().isEmpty() && !item.recoveryAction().isBlank()));
  }

  @Test
  void scanFilesShouldOnlyScanRequestedFiles() throws IOException {
    write(Map.of("src/main/java/Ignored.java", "throw new IllegalStateException();",
        "src/main/java/Scanned.java", "throw new IllegalArgumentException();"));

    List<CodeSignalEvidence> evidences = service.scanFiles(repository,
        List.of("src/main/java/Scanned.java"), "scan-1");

    assertTrue(evidences.stream()
        .allMatch(item -> item.filePath().equals("src/main/java/Scanned.java")));
    assertFalse(evidences.isEmpty());
  }

  @Test
  void scanFilesShouldFilterCommentLinesInDependencies() throws IOException {
    List<String> files = write(Map.of("src/main/java/Service.java",
        "// https://ignore.example.com\n# https://ignore-hash.com\n"
            + "/* https://ignore-block.com */\nString kafka = \"events\";\n"));

    List<CodeSignalEvidence> evidences = service.scanFiles(repository, files, "scan-1");

    assertFalse(evidences.stream().anyMatch(item -> item.matchText().contains("ignore")));
    assertTrue(evidences.stream().anyMatch(item -> item.signalType().equals("DEPENDENCY")
        && item.matchText().contains("kafka")));
  }

  @Test
  void scanFilesShouldFilterTrivialConfigMatches() throws IOException {
    List<String> files = write(Map.of("src/main/resources/application.yml",
        "short: ${}\ntiny: ${a}\nvalid_timeout: 5000\nenv: ${DATABASE_URL:localhost}\n"));

    List<CodeSignalEvidence> evidences = service.scanFiles(repository, files, "scan-1");

    assertFalse(evidences.stream().anyMatch(item -> item.signalType().equals("CONFIG")
        && item.matchText().length() < 8));
    assertTrue(evidences.stream().anyMatch(item -> item.signalType().equals("CONFIG")
        && item.matchText().contains("DATABASE_URL")));
  }

  @Test
  void scanFilesShouldDetectExpandedTechnologies() throws IOException {
    List<String> files = write(Map.of("k8s/deployment.yaml", "kind: Deployment\nredis: 6379\n",
        "src/main/java/Cloud.java", "String azure = \"azure-blob\";\nString aws = \"aws-sqs\";\n"));

    List<CodeSignalEvidence> evidences = service.scanFiles(repository, files, "scan-1");

    assertTrue(List.of("azure", "aws", "kubernetes").stream().allMatch(technology ->
        evidences.stream().anyMatch(item -> item.technology().equals(technology))));
  }

  @Test
  void scanFilesShouldReadConfigurationFormatsDeclaredByTheConfigRule() throws IOException {
    List<String> files = write(Map.of("config/app.toml", "timeout = 3000 # payments\n",
        "config/.env.conf", "retry_attempts=3 for payments\n"));

    List<CodeSignalEvidence> evidences = service.scanFiles(repository, files, "scan-1");

    assertEquals(2, evidences.stream().filter(item -> item.signalType().equals("CONFIG"))
        .count());
  }

  @Test
  void scanFilesShouldProduceStableEvidenceIdsForUnchangedContent() throws IOException {
    List<String> files = write(Map.of("src/main/java/Api.java", "@GetMapping(\"/payments\")"));

    List<String> first = ids(service.scanFiles(repository, files, "scan-1"));
    List<String> second = ids(service.scanFiles(repository, files, "scan-2"));

    assertEquals(first, second);
  }

  @Test
  void isScannableShouldSkipIgnoredDirectoriesAndBinaryFiles() {
    assertTrue(service.isScannable("src/main/java/App.java"));
    assertFalse(service.isScannable("build/generated/App.java"));
    assertFalse(service.isScannable("node_modules/lib/index.js"));
    assertFalse(service.isScannable("docs/diagram.png"));
  }

  @Test
  void scanFilesShouldCaptureOpenApiBusinessDescriptionsExceptBlockScalars() throws IOException {
    List<String> files = write(Map.of("src/main/resources/openapi.yaml", """
        info:
          title: Payments API
          description: |
            Multi line text
        paths:
          /payments:
            post:
              summary: Registers a customer payment
              operationId: createPayment
        """));

    List<String> descriptions = matchTexts(service.scanFiles(repository, files, "scan-1"),
        "API_OPENAPI_DOCUMENTATION");

    assertEquals(List.of("operationId: createPayment", "summary: Registers a customer payment",
        "title: Payments API"), descriptions);
  }

  @Test
  void scanFilesShouldCaptureErrorCatalogMessages() throws IOException {
    List<String> files = write(Map.of(
        "src/main/java/com/example/ErrorCode.java", """
            public enum ErrorCode {
              CUSTOMER_NOT_FOUND("CUS-404", "The customer does not exist"),
              INVALID("x");
            }
            """,
        "src/main/resources/messages.properties", """
            # payment.ignored=This comment must not be captured
            payment.rejected=The payment was rejected by the bank
            """));

    List<String> messages = matchTexts(service.scanFiles(repository, files, "scan-1"),
        "EXCEPTION_MESSAGE_CATALOG");

    assertEquals(List.of(
        "CUSTOMER_NOT_FOUND(\"CUS-404\", \"The customer does not exist\"),",
        "payment.rejected=The payment was rejected by the bank"), messages);
  }

  @Test
  void scanFilesShouldCaptureServicePurposeFromReadmeAndDescriptor() throws IOException {
    List<String> files = write(Map.of(
        "README.md", """
            # Payments Service
            Registers and settles customer payments.
            ```bash
            echo "code must be skipped"
            ```
            ![diagram](docs/diagram.png)
            """,
        "pom.xml", "<project>\n  <description>Retail payments service</description>\n"
            + "</project>"));

    List<CodeSignalEvidence> overview = service.scanFiles(repository, files, "scan-1").stream()
        .filter(item -> item.signalType().equals("BUSINESS_CONTEXT"))
        .toList();

    assertEquals(List.of("# Payments Service", "Registers and settles customer payments.",
        "<description>Retail payments service</description>"),
        overview.stream().map(CodeSignalEvidence::matchText).toList());
  }

  private static RepositoryCodeScanService scanService() {
    FileTypeResolver fileTypeResolver = new FileTypeResolver();
    List<SignalDetector> detectors = List.of(new ApiSignalDetector(),
        new DatabaseSignalDetector(), new ExceptionSignalDetector(), new ConfigSignalDetector(),
        new DependencySignalDetector(), new ApiDocumentationSignalDetector(),
        new ErrorCatalogSignalDetector(), new ServiceOverviewSignalDetector());
    return new RepositoryCodeScanService(detectors, new SourceFileReader(fileTypeResolver),
        fileTypeResolver, new EvidenceFactory(new SymbolLocator(), new SignalClassifier()));
  }

  private List<String> matchTexts(List<CodeSignalEvidence> evidences, String matchedRule) {
    return evidences.stream().filter(item -> item.matchedRule().equals(matchedRule))
        .map(CodeSignalEvidence::matchText).sorted().toList();
  }

  private List<String> write(Map<String, String> files) throws IOException {
    for (Map.Entry<String, String> file : files.entrySet()) {
      Path path = repository.resolve(file.getKey());
      Files.createDirectories(path.getParent());
      Files.writeString(path, file.getValue());
    }
    return List.copyOf(files.keySet());
  }

  private List<String> ids(List<CodeSignalEvidence> evidences) {
    return evidences.stream().map(CodeSignalEvidence::evidenceId).sorted().toList();
  }
}
