package com.pichincha.cdemsaspopenaidocumentation.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pichincha.cdemsaspopenaidocumentation.domain.CodeScanSummary;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class RepositoryCodeScanServiceTest {

  @Test
  void scanRepositoryShouldCountSourceArtifacts() throws Exception {
    Path repository = Files.createTempDirectory("scan-test");
    Files.createDirectories(repository.resolve("src/main/java"));
    Files.createDirectories(repository.resolve("docs"));
    Files.writeString(repository.resolve("src/main/java/App.java"), "class App {}");
    Files.writeString(repository.resolve("application.yml"), "spring:\n  app: test");
    Files.writeString(repository.resolve("docs/readme.md"), "# test");
    Files.writeString(repository.resolve("docs/values.yaml"), "name: value");

    RepositoryCodeScanService service = new RepositoryCodeScanService();
    CodeScanSummary summary = service.scanRepository(repository.toString());

    assertEquals(4L, summary.totalFiles());
    assertEquals(1L, summary.javaFiles());
    assertEquals(2L, summary.yamlFiles());
    assertEquals(1L, summary.markdownFiles());
    assertEquals(1L, summary.filesByLanguage().get("java"));
    assertEquals(2L, summary.filesByLanguage().get("yaml"));
    assertEquals(1L, summary.filesByExtension().get("md"));
    assertEquals("FULL", summary.metadata().scanMode());
  }

  @Test
  void scanRepositoryShouldExtractApiDatabaseAndExceptionIndicators() throws Exception {
    Path repository = Files.createTempDirectory("scan-indicators-test");
    Files.createDirectories(repository.resolve("src/main/java/com/example/controller"));
    Files.createDirectories(repository.resolve("src/main/resources"));
    Files.writeString(repository.resolve("src/main/java/com/example/controller/AppController.java"),
        "@RequestMapping(\"/api/v1\")\n"
            + "class AppController {\n"
            + "  @GetMapping(\"/customers\")\n"
            + "  void method() { throw new IllegalArgumentException(); }\n"
            + "}");
    Files.writeString(repository.resolve("src/main/java/com/example/UserEntity.java"),
        "@Entity class UserEntity {}");
    Files.writeString(repository.resolve("src/main/resources/application.yml"),
        "spring:\n  datasource:\n    url: ${DB_URL:jdbc:postgresql://localhost:5432/test}\n"
            + "app:\n  timeout: 5000");
    Files.writeString(repository.resolve("src/main/resources/openapi.yml"),
        "info:\n  description: redistribution rules for cash");

    RepositoryCodeScanService service = new RepositoryCodeScanService();
    CodeScanSummary summary = service.scanRepository(repository.toString());

    assertTrue(summary.apiIndicators().stream().anyMatch(value -> value.contains("@GetMapping")));
    assertTrue(summary.databaseIndicators().stream().anyMatch(value -> value.contains("jdbc:")));
    assertTrue(summary.databaseIndicators().stream().anyMatch(value -> value.contains("@Entity")));
    assertTrue(summary.exceptionIndicators().stream().anyMatch(value -> value.contains("throw new")));
    assertFalse(summary.databaseIndicators().stream()
        .anyMatch(value -> value.toLowerCase().contains("redistribution")));
    assertFalse(summary.evidences().isEmpty());
    assertTrue(summary.evidences().stream().anyMatch(value -> value.signalType().equals("API")));
    assertTrue(summary.evidences().stream().anyMatch(value -> value.signalType().equals("DATABASE")));
    assertTrue(summary.evidences().stream().anyMatch(value -> value.signalType().equals("EXCEPTION")));
    assertTrue(summary.evidences().stream().anyMatch(value -> value.signalType().equals("CONFIG")));
    assertTrue(summary.evidences().stream()
      .anyMatch(value -> value.signalType().equals("DEPENDENCY")));
    assertTrue(summary.evidences().stream()
      .allMatch(value -> !value.snippet().isEmpty() && value.startLine() <= value.endLine()));
    assertTrue(summary.evidences().stream().allMatch(value -> !value.matchedRule().isBlank()));
    assertTrue(summary.evidences().stream().allMatch(value -> !value.matchText().isBlank()));
    assertTrue(summary.evidences().stream().allMatch(value -> !value.language().isBlank()));
    assertTrue(summary.evidences().stream().allMatch(value -> !value.sourceKind().isBlank()));
    assertTrue(summary.evidences().stream().allMatch(value -> !value.ruleId().isBlank()));
    assertTrue(summary.evidences().stream().allMatch(value -> !value.severityHint().isBlank()));
    assertTrue(summary.evidences().stream().allMatch(value -> !value.runtimeSurface().isBlank()));
    assertTrue(summary.evidences().stream().allMatch(value -> !value.dependencySurface().isEmpty()));
    assertTrue(summary.evidences().stream().allMatch(value -> !value.scopeSymbol().isBlank()));
    assertTrue(summary.evidences().stream().allMatch(value -> !value.evidenceId().isBlank()));
    assertTrue(summary.evidences().stream().allMatch(value -> !value.probableCause().isBlank()));
    assertTrue(summary.evidences().stream().allMatch(value -> !value.recoveryAction().isBlank()));
    assertTrue(summary.apiIndicators().stream()
      .allMatch(value -> value.contains("id=") && value.contains("ruleId=")
          && value.contains("lang=") && value.contains("rule=")
          && value.contains("scope=") && value.contains("symbol=")));
    assertEquals(summary.apiIndicators().size(), summary.metadata().apiSignalsExtracted());
    assertEquals(summary.databaseIndicators().size(),
      summary.metadata().databaseSignalsExtracted());
    assertEquals(summary.exceptionIndicators().size(),
      summary.metadata().exceptionSignalsExtracted());
    assertTrue(summary.metadata().configSignalsExtracted() >= 1L);
    assertTrue(summary.metadata().dependencySignalsExtracted() >= 1L);
    assertTrue(summary.metadata().elapsedMs() >= 0L);
    assertTrue(summary.metadata().filesScanned() >= 1L);
  }
}
