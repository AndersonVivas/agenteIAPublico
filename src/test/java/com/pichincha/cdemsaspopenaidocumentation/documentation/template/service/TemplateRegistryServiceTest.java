package com.pichincha.cdemsaspopenaidocumentation.documentation.template.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.RawTemplateDescriptor;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateFormat;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateRegistrationResult;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateSourceType;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateVersion;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.port.ExternalTemplateSource;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class TemplateRegistryServiceTest {

  @Test
  void shouldRegisterOnlyValidTemplates() {
    RawTemplateDescriptor valid = new RawTemplateDescriptor("error.yaml",
        "error-manual", "Error Manual", new TemplateVersion("1.0.0"), "x",
        TemplateFormat.YAML, TemplateSourceType.LOCAL, "/tmp/error.yaml", "",
        Instant.now());
    RawTemplateDescriptor invalid = new RawTemplateDescriptor("invalid.txt",
        "invalid", "Invalid", new TemplateVersion("1.0.0"), "x",
        TemplateFormat.UNKNOWN, TemplateSourceType.LOCAL, "/tmp/invalid.txt", "",
        Instant.now());
    CompositeTemplateSource source = new CompositeTemplateSource(List.of(
        sourceOf(List.of(valid, invalid))));
    TemplateRegistryService registry = new TemplateRegistryService(source,
        new TemplateValidator(), new TemplateCatalog());

    TemplateRegistrationResult result = registry.registerAvailableTemplates();

    assertEquals(2, result.discoveredCount());
    assertEquals(1, result.validCount());
    assertEquals(1, result.publishedCount());
    assertEquals(1, result.rejectedCount());
  }

  private ExternalTemplateSource sourceOf(List<RawTemplateDescriptor> descriptors) {
    return new ExternalTemplateSource() {
      @Override
      public TemplateSourceType sourceType() {
        return TemplateSourceType.LOCAL;
      }

      @Override
      public List<RawTemplateDescriptor> listAvailable() {
        return descriptors;
      }

      @Override
      public Optional<RawTemplateDescriptor> findBySourceId(String sourceId) {
        return descriptors.stream().filter(descriptor ->
            descriptor.sourceId().equals(sourceId)).findFirst();
      }
    };
  }
}
