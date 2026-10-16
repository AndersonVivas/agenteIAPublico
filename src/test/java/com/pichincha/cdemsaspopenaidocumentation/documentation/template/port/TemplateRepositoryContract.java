package com.pichincha.cdemsaspopenaidocumentation.documentation.template.port;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pichincha.cdemsaspopenaidocumentation.documentation.audience.domain.AudienceType;
import com.pichincha.cdemsaspopenaidocumentation.documentation.communication.domain.NarrativeStyle;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.DocumentTemplate;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateMetadata;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateVersion;
import java.util.List;
import org.junit.jupiter.api.Test;

abstract class TemplateRepositoryContract {

  protected abstract TemplateRepository repository();

  @Test
  void shouldResolveActiveTemplateWithHighestVersion() {
    DocumentTemplate template = repository().resolveActive("error-manual").orElseThrow();

    assertEquals("error-manual", template.id());
    assertEquals("1.1.0", template.version().value());
  }

  @Test
  void shouldReturnEmptyWhenTemplateDoesNotExist() {
    assertTrue(repository().resolveActive("unknown-template").isEmpty());
  }

  @Test
  void shouldListActiveTemplateIds() {
    assertEquals(List.of("error-manual"), repository().activeTemplateIds());
  }

  @Test
  void shouldResolveDefaultAudienceFromActiveTemplate() {
    DocumentTemplate template = repository().resolveActive("error-manual").orElseThrow();

    assertEquals(AudienceType.OPERATIONS, template.reader().audience().audienceType());
  }

  @Test
  void shouldResolveCommunicationProfileFromActiveTemplate() {
    DocumentTemplate template = repository().resolveActive("error-manual").orElseThrow();

    assertEquals(NarrativeStyle.OPERATIONAL,
        template.reader().communication().narrativeStyle());
  }

  protected DocumentTemplate template(String id, String name, String version) {
    return new DocumentTemplate(id, name, new TemplateVersion(version), List.of(),
        TemplateMetadata.internal());
  }
}
