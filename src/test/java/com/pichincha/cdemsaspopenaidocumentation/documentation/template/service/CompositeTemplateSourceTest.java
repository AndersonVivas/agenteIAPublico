package com.pichincha.cdemsaspopenaidocumentation.documentation.template.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.RawTemplateDescriptor;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateFormat;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateVersion;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.port.ExternalTemplateSource;
import java.util.List;
import org.junit.jupiter.api.Test;

class CompositeTemplateSourceTest {

  @Test
  void shouldDeduplicateByTemplateAndVersion() {
    RawTemplateDescriptor descriptor = new RawTemplateDescriptor("a/error.yaml",
        "error-manual", "Error Manual", new TemplateVersion("1.0.0"), "x",
        TemplateFormat.YAML, "");
    ExternalTemplateSource left = source(descriptor);
    ExternalTemplateSource right = source(descriptor);
    CompositeTemplateSource composite = new CompositeTemplateSource(List.of(left, right));

    List<RawTemplateDescriptor> available = composite.listAvailable();

    assertEquals(1, available.size());
  }

  private ExternalTemplateSource source(RawTemplateDescriptor descriptor) {
    return new ExternalTemplateSource() {
      @Override
      public List<RawTemplateDescriptor> listAvailable() {
        return List.of(descriptor);
      }
    };
  }
}
