package com.pichincha.cdemsaspopenaidocumentation.copilot.support;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.ChangelogEntry;
import com.pichincha.cdemsaspopenaidocumentation.domain.enums.DocumentLifecycleEnum;
import com.pichincha.cdemsaspopenaidocumentation.domain.enums.ScanScopeEnum;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class ChangelogMarkdownRendererTest {

  @Test
  void renderShouldListVersionsWithScopeEvidenceAndUpdatedSections() {
    String markdown = ChangelogMarkdownRenderer.render("Error Manual", List.of(
        entry(2, ScanScopeEnum.INCREMENTAL, List.of("Catalogo de Errores")),
        entry(1, ScanScopeEnum.FULL, List.of())));

    assertTrue(markdown.startsWith("# Changelog - Error Manual"));
    assertTrue(markdown.indexOf("## Version 2") < markdown.indexOf("## Version 1"));
    assertTrue(markdown.contains("- Alcance del escaneo: INCREMENTAL (3 archivos cambiados)"));
    assertTrue(markdown.contains("- Secciones actualizadas: Catalogo de Errores"));
    assertTrue(markdown.contains("- Pull Request: #7"));
  }

  private ChangelogEntry entry(int version, ScanScopeEnum scope, List<String> sections) {
    return new ChangelogEntry("org/project/repo/error-manual", "org/project/repo",
        "error-manual", DocumentLifecycleEnum.EVOLUTIONARY, version, "/out/error-manual.md",
        "scan-" + version, 7L, "abc123", scope, 3, 5, 1, sections, List.of(),
        Instant.parse("2026-10-16T10:15:30Z"));
  }
}
