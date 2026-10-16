package com.pichincha.cdemsaspopenaidocumentation.documentation.template.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.RawTemplateDescriptor;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateFormat;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateVersion;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.port.ExternalTemplateSource;
import java.util.List;
import org.junit.jupiter.api.Test;

class TemplateRegistryServiceTest {

  @Test
  void shouldRegisterOnlyValidTemplates() {
    RawTemplateDescriptor valid = new RawTemplateDescriptor("error.yaml",
        "error-manual", "Error Manual", new TemplateVersion("1.0.0"), "x",
        TemplateFormat.YAML, "");
    RawTemplateDescriptor invalid = new RawTemplateDescriptor("invalid.txt",
        "invalid", "Invalid", new TemplateVersion("1.0.0"), "x",
        TemplateFormat.UNKNOWN, "");
    CompositeTemplateSource source = new CompositeTemplateSource(List.of(
        sourceOf(List.of(valid, invalid))));
    TemplateCatalog catalog = new TemplateCatalog();
    TemplateRegistryService registry = new TemplateRegistryService(source,
        new TemplateValidator(new TemplateGuidanceParser(),
        new TemplateReaderParser(new TemplateCommunicationParser())), catalog);

    registry.registerAvailableTemplates();

    assertEquals(List.of("error-manual"), catalog.activeTemplateIds());
  }

  private ExternalTemplateSource sourceOf(List<RawTemplateDescriptor> descriptors) {
    return new ExternalTemplateSource() {
      @Override
      public List<RawTemplateDescriptor> listAvailable() {
        return descriptors;
      }
    };
  }
}
