package com.pichincha.cdemsaspopenaidocumentation.documentation.template.port;

import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.DocumentTemplate;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateResolutionResult;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateVersion;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

class InMemoryTemplateRepositoryContractTest extends TemplateRepositoryContract {

  @Override
  protected TemplateRepository repository() {
    DocumentTemplate errorManualV1 = template("error-manual", "Error Manual", "1.0.0");
    DocumentTemplate errorManualV2 = template("error-manual", "Error Manual", "1.1.0");
    return new InMemoryTemplateRepository(List.of(errorManualV1, errorManualV2));
  }

  private static final class InMemoryTemplateRepository implements TemplateRepository {

    private final Map<String, List<DocumentTemplate>> templatesById;

    private InMemoryTemplateRepository(List<DocumentTemplate> templates) {
      templatesById = templates.stream().collect(Collectors.groupingBy(
          DocumentTemplate::id, LinkedHashMap::new, Collectors.toList()));
    }

    @Override
    public Optional<TemplateResolutionResult> resolveActive(String templateId) {
      return versions(templateId).stream().max(Comparator.comparing(
          template -> template.version().value())).map(template ->
              TemplateResolutionResult.resolved(template, "in-memory"));
    }

    @Override
    public Optional<TemplateResolutionResult> resolveVersion(String templateId,
        TemplateVersion version) {
      return versions(templateId).stream().filter(template ->
          template.version().equals(version)).findFirst().map(template ->
              TemplateResolutionResult.resolved(template, "in-memory"));
    }

    @Override
    public List<TemplateResolutionResult> searchByName(String templateName) {
      String query = templateName == null ? "" : templateName.toLowerCase(Locale.ROOT);
      return templatesById.values().stream().flatMap(List::stream).filter(template ->
          template.name().toLowerCase(Locale.ROOT).contains(query)).map(template ->
              TemplateResolutionResult.resolved(template, "in-memory")).toList();
    }

    private List<DocumentTemplate> versions(String templateId) {
      return templatesById.getOrDefault(templateId, List.of());
    }
  }
}
