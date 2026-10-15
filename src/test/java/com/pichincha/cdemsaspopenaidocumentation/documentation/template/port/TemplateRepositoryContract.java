package com.pichincha.cdemsaspopenaidocumentation.documentation.template.port;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pichincha.cdemsaspopenaidocumentation.documentation.audience.domain
  .AudienceType;
import com.pichincha.cdemsaspopenaidocumentation.documentation.audience.service
  .AudienceProfileResolver;
import com.pichincha.cdemsaspopenaidocumentation.documentation.communication.domain
  .NarrativeStyle;
import com.pichincha.cdemsaspopenaidocumentation.documentation.communication.service
  .CommunicationProfileResolver;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.DocumentTemplate;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateMetadata;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateResolutionResult;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateVersion;
import java.util.List;
import org.junit.jupiter.api.Test;

abstract class TemplateRepositoryContract {

  protected abstract TemplateRepository repository();

  @Test
  void shouldResolveActiveTemplate() {
    TemplateRepository repository = repository();

    TemplateResolutionResult result = repository.resolveActive("error-manual")
        .orElseThrow();

    assertEquals("error-manual", result.template().id());
    assertFalse(result.fallbackApplied());
  }

  @Test
  void shouldResolveTemplateByVersion() {
    TemplateRepository repository = repository();

    TemplateResolutionResult result = repository.resolveVersion("error-manual",
        new TemplateVersion("1.0.0")).orElseThrow();

    assertEquals("1.0.0", result.resolvedVersion().value());
  }

  @Test
  void shouldReturnEmptyWhenTemplateDoesNotExist() {
    TemplateRepository repository = repository();

    assertTrue(repository.resolveActive("unknown-template").isEmpty());
    assertTrue(repository.resolveVersion("unknown-template",
        new TemplateVersion("9.9.9")).isEmpty());
  }

  @Test
  void shouldSearchTemplatesByName() {
    TemplateRepository repository = repository();

    List<TemplateResolutionResult> results = repository.searchByName("manual");

    assertFalse(results.isEmpty());
    assertTrue(results.stream().allMatch(result ->
        "Error Manual".equals(result.template().name())));
  }

  @Test
  void shouldResolveDefaultAudienceFromActiveTemplate() {
    TemplateRepository repository = repository();
    AudienceProfileResolver resolver = new AudienceProfileResolver();

    AudienceType resolvedType = resolver.resolveFromRepository(repository,
        "error-manual", null).orElseThrow().resolvedType();

    assertEquals(AudienceType.OPERATIONS, resolvedType);
  }

  @Test
  void shouldResolveCommunicationProfileFromActiveTemplate() {
    TemplateRepository repository = repository();
    CommunicationProfileResolver resolver = new CommunicationProfileResolver();

    NarrativeStyle style = resolver.resolveFromRepository(repository,
        "error-manual", null).orElseThrow().narrativeStyle();

    assertEquals(NarrativeStyle.OPERATIONAL, style);
  }

  protected DocumentTemplate template(String id, String name, String version) {
    return new DocumentTemplate(id, name, new TemplateVersion(version), List.of(),
        TemplateMetadata.internal());
  }
}
