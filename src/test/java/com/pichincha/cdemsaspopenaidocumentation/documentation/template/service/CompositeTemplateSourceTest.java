package com.pichincha.cdemsaspopenaidocumentation.documentation.template.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.RawTemplateDescriptor;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateFormat;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateSourceType;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateVersion;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.port.ExternalTemplateSource;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class CompositeTemplateSourceTest {

  @Test
  void shouldDeduplicateByTemplateAndVersion() {
    RawTemplateDescriptor descriptor = new RawTemplateDescriptor("a/error.yaml",
        "error-manual", "Error Manual", new TemplateVersion("1.0.0"), "x",
        TemplateFormat.YAML, TemplateSourceType.LOCAL, "", "", Instant.now());
    ExternalTemplateSource left = source(descriptor);
    ExternalTemplateSource right = source(descriptor);
    CompositeTemplateSource composite = new CompositeTemplateSource(List.of(left, right));

    List<RawTemplateDescriptor> available = composite.listAvailable();

    assertEquals(1, available.size());
  }

  private ExternalTemplateSource source(RawTemplateDescriptor descriptor) {
    return new ExternalTemplateSource() {
      @Override
      public TemplateSourceType sourceType() {
        return TemplateSourceType.LOCAL;
      }

      @Override
      public List<RawTemplateDescriptor> listAvailable() {
        return List.of(descriptor);
      }

      @Override
      public Optional<RawTemplateDescriptor> findBySourceId(String sourceId) {
        return Optional.of(descriptor);
      }
    };
  }
}
