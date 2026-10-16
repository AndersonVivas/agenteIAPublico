package com.pichincha.cdemsaspopenaidocumentation.documentation.template.adapter.filesystem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pichincha.cdemsaspopenaidocumentation.documentation.config.DocumentationTemplatesProperties;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.RawTemplateDescriptor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileSystemTemplateSourceTest {

  @TempDir
  Path tempDir;

  @Test
  void shouldDiscoverYamlJsonAndMarkdownTemplates() throws Exception {
    Files.writeString(tempDir.resolve("error-manual-1.2.0.yaml"),
        "id: error-manual\nname: Error Manual\nversion: 1.2.0\n");
    Files.writeString(tempDir.resolve("api-template.json"),
        "{\"templateId\":\"api-template\",\"name\":\"API\",\"version\":\"2.0.0\"}");
    Files.writeString(tempDir.resolve("overview.md"),
        "---\ntemplateId: overview\nname: Overview\n---\n\n## Summary\n");

    FileSystemTemplateSource source = source();

    List<RawTemplateDescriptor> descriptors = source.listAvailable();

    assertEquals(3, descriptors.size());
    assertTrue(descriptors.stream().noneMatch(descriptor -> descriptor.checksum().isBlank()));
    assertTrue(descriptors.stream().anyMatch(descriptor ->
        descriptor.templateId().equals("error-manual")
            && descriptor.declaredVersion().value().equals("1.2.0")));
  }

  @Test
  void shouldResolveVersionFromParentFolderWhenMissingInContent() throws Exception {
    Path versionFolder = Files.createDirectories(tempDir.resolve("v3.4.5"));
    Files.writeString(versionFolder.resolve("runbook.yaml"),
        "id: runbook\nname: Runbook\n");

    FileSystemTemplateSource source = source();

    RawTemplateDescriptor descriptor = source.listAvailable().getFirst();

    assertEquals("3.4.5", descriptor.declaredVersion().value());
    assertEquals("runbook", descriptor.templateId());
  }

  private FileSystemTemplateSource source() {
    DocumentationTemplatesProperties properties =
        new DocumentationTemplatesProperties(tempDir.toString(),
            List.of(".yaml", ".json", ".md"));
    TemplateDirectoryScanner scanner = new TemplateDirectoryScanner(properties);
    TemplateFileResolver resolver = new TemplateFileResolver(
        new TemplateVersionResolver());
    return new FileSystemTemplateSource(scanner, resolver);
  }
}