package com.pichincha.cdemsaspopenaidocumentation.documentation.template.service;

import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.DocumentTemplate;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateCatalogEntry;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateResolutionResult;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateSourceType;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateVersion;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.support.TemplateVersionComparator;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class TemplateCatalog {

  private final TemplateVersionComparator versionComparator;
  private final Map<String, Map<TemplateVersion, TemplateCatalogEntry>> catalog;

  public TemplateCatalog() {
    this.versionComparator = new TemplateVersionComparator();
    this.catalog = new ConcurrentHashMap<>();
  }

  public synchronized boolean publish(DocumentTemplate template, String publishedFrom,
      TemplateSourceType sourceType, boolean fallbackEligible) {
    if (template == null || StringUtils.isBlank(template.id())) {
      return false;
    }
    Map<TemplateVersion, TemplateCatalogEntry> versions =
        catalog.computeIfAbsent(template.id(), key -> new LinkedHashMap<>());
    if (versions.containsKey(template.version())) {
      return false;
    }
    deactivate(versions);
    TemplateCatalogEntry entry = new TemplateCatalogEntry(template, template.version(),
        publishedFrom, sourceType, Instant.now(), true, fallbackEligible);
    versions.put(template.version(), entry);
    return true;
  }

  public synchronized Optional<TemplateResolutionResult> resolveActive(String templateId) {
    return versions(templateId).stream().filter(TemplateCatalogEntry::active)
        .findFirst().map(this::toResult);
  }

  public synchronized Optional<TemplateResolutionResult> resolveVersion(String templateId,
      TemplateVersion version) {
    return Optional.ofNullable(catalog.getOrDefault(templateId, Map.of()).get(version))
        .map(this::toResult);
  }

  public synchronized List<TemplateResolutionResult> searchByName(String templateName) {
    String query = templateName == null ? "" : templateName.trim().toLowerCase();
    return catalog.values().stream().flatMap(map -> map.values().stream())
        .filter(entry -> entry.template().name().toLowerCase().contains(query))
        .sorted(Comparator.comparing(entry -> entry.template().id()))
        .map(this::toResult).toList();
  }

  public synchronized List<TemplateCatalogEntry> entries() {
    return catalog.values().stream().flatMap(map -> map.values().stream())
        .sorted(Comparator.comparing((TemplateCatalogEntry entry) -> entry.template().id())
            .thenComparing(TemplateCatalogEntry::version, versionComparator))
        .collect(Collectors.toCollection(ArrayList::new));
  }

  private List<TemplateCatalogEntry> versions(String templateId) {
    return catalog.getOrDefault(templateId, Map.of()).values().stream()
        .sorted(Comparator.comparing(TemplateCatalogEntry::version, versionComparator)
            .reversed()).toList();
  }

  private void deactivate(Map<TemplateVersion, TemplateCatalogEntry> versions) {
    versions.replaceAll((version, entry) -> new TemplateCatalogEntry(entry.template(),
        version, entry.publishedFrom(), entry.sourceType(), entry.registeredAt(),
        false, entry.fallbackEligible()));
  }

  private TemplateResolutionResult toResult(TemplateCatalogEntry entry) {
    return new TemplateResolutionResult(entry.template(), entry.version(),
        entry.publishedFrom(), false);
  }
}
