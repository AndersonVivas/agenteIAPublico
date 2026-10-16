package com.pichincha.cdemsaspopenaidocumentation.documentation.template.service;

import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.DocumentTemplate;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.RawTemplateDescriptor;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateValidationResult;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TemplateRegistryService {

  private final CompositeTemplateSource templateSource;
  private final TemplateValidator templateValidator;
  private final TemplateCatalog templateCatalog;

  public void registerAvailableTemplates() {
    List<RawTemplateDescriptor> descriptors = templateSource.listAvailable();
    List<DocumentTemplate> validTemplates = new ArrayList<>();
    Map<String, List<String>> errors = new LinkedHashMap<>();
    for (RawTemplateDescriptor descriptor : descriptors) {
      TemplateValidationResult validation = templateValidator.validate(descriptor);
      if (validation.valid()) {
        validTemplates.add(validation.normalizedTemplate());
        validation.warnings().forEach(warning ->
            log.warn("Template {}: {}", descriptor.sourceId(), warning));
      } else {
        errors.put(descriptor.sourceId(), validation.errors());
      }
    }
    errors.forEach((source, messages) -> log.warn("Template {} rejected: {}", source, messages));
    templateCatalog.replaceAll(validTemplates);
  }
}
