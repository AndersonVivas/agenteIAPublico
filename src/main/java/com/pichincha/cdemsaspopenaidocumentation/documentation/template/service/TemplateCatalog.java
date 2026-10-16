package com.pichincha.cdemsaspopenaidocumentation.documentation.template.service;

import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.DocumentTemplate;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.support.TemplateVersionComparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class TemplateCatalog {

  private final TemplateVersionComparator versionComparator = new TemplateVersionComparator();
  private final AtomicReference<Map<String, DocumentTemplate>> activeTemplates =
      new AtomicReference<>(Map.of());

  public void replaceAll(List<DocumentTemplate> templates) {
    Map<String, DocumentTemplate> latestById = templates.stream()
        .collect(Collectors.toMap(DocumentTemplate::id, template -> template,
            this::latestVersion, TreeMap::new));
    latestById.values().removeIf(template -> !template.metadata().active());
    activeTemplates.set(Map.copyOf(latestById));
  }

  public Optional<DocumentTemplate> resolveActive(String templateId) {
    return Optional.ofNullable(activeTemplates.get().get(templateId));
  }

  public List<String> activeTemplateIds() {
    return activeTemplates.get().keySet().stream().sorted().toList();
  }

  private DocumentTemplate latestVersion(DocumentTemplate first, DocumentTemplate second) {
    return versionComparator.compare(second.version(), first.version()) > 0 ? second : first;
  }
}
