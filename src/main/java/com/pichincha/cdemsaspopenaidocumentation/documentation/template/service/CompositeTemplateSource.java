package com.pichincha.cdemsaspopenaidocumentation.documentation.template.service;

import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.RawTemplateDescriptor;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.port.ExternalTemplateSource;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class CompositeTemplateSource {

  private final List<ExternalTemplateSource> sources;

  public CompositeTemplateSource(List<ExternalTemplateSource> sources) {
    this.sources = List.copyOf(sources);
  }

  public List<RawTemplateDescriptor> listAvailable() {
    Map<String, RawTemplateDescriptor> merged = new LinkedHashMap<>();
    for (ExternalTemplateSource source : sources) {
      for (RawTemplateDescriptor descriptor : source.listAvailable()) {
        merged.putIfAbsent(dedupKey(descriptor), descriptor);
      }
    }
    return merged.values().stream().toList();
  }

  public Optional<RawTemplateDescriptor> findBySourceId(String sourceId) {
    return listAvailable().stream().filter(descriptor ->
        descriptor.sourceId().equals(sourceId)).findFirst();
  }

  private String dedupKey(RawTemplateDescriptor descriptor) {
    return descriptor.templateId() + "::" + descriptor.declaredVersion().value();
  }
}
