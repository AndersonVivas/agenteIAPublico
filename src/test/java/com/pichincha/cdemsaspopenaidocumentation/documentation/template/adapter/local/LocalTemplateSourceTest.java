package com.pichincha.cdemsaspopenaidocumentation.documentation.template.adapter.local;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pichincha.cdemsaspopenaidocumentation.documentation.config.TemplatePlatformProperties;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.RawTemplateDescriptor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LocalTemplateSourceTest {

  @TempDir
  Path tempDir;

  @Test
  void shouldReadSupportedTemplateFiles() throws Exception {
    Path yaml = tempDir.resolve("error-manual.yaml");
    Path markdown = tempDir.resolve("api.md");
    Path ignored = tempDir.resolve("notes.txt");
    Files.writeString(yaml, "id: error-manual");
    Files.writeString(markdown, "# api");
    Files.writeString(ignored, "ignored");
    TemplatePlatformProperties properties = new TemplatePlatformProperties(null,
      false, false, false, true, tempDir.toString(),
      List.of(".yaml", ".md"));
    LocalTemplateSource source = new LocalTemplateSource(properties);

    List<RawTemplateDescriptor> descriptors = source.listAvailable();

    assertEquals(2, descriptors.size());
    assertTrue(descriptors.stream().allMatch(descriptor ->
        descriptor.sourceId().endsWith(".yaml") || descriptor.sourceId().endsWith(".md")));
  }

  @Test
  void shouldReturnEmptyWhenDisabled() {
    TemplatePlatformProperties properties = new TemplatePlatformProperties(null,
        false, false, false, false, tempDir.toString(),
        List.of(".yaml"));
    LocalTemplateSource source = new LocalTemplateSource(properties);

    assertTrue(source.listAvailable().isEmpty());
  }
}
