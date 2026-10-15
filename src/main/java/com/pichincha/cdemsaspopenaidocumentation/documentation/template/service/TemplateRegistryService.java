package com.pichincha.cdemsaspopenaidocumentation.documentation.template.service;

import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.DocumentTemplate;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.RawTemplateDescriptor;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateRegistrationResult;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateValidationResult;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class TemplateRegistryService {

  private final CompositeTemplateSource templateSource;
  private final TemplateValidator templateValidator;
  private final TemplateCatalog templateCatalog;

  public TemplateRegistryService(CompositeTemplateSource templateSource,
      TemplateValidator templateValidator, TemplateCatalog templateCatalog) {
    this.templateSource = templateSource;
    this.templateValidator = templateValidator;
    this.templateCatalog = templateCatalog;
  }

  public TemplateRegistrationResult registerAvailableTemplates() {
    List<RawTemplateDescriptor> descriptors = templateSource.listAvailable();
    List<String> publishedIds = new ArrayList<>();
    Map<String, List<String>> errors = new LinkedHashMap<>();
    int validCount = 0;
    int publishedCount = 0;
    for (RawTemplateDescriptor descriptor : descriptors) {
      TemplateValidationResult validation = templateValidator.validate(descriptor);
      if (!validation.valid()) {
        errors.put(descriptor.sourceId(), validation.errors());
        continue;
      }
      validCount++;
      if (publish(descriptor, validation.normalizedTemplate())) {
        publishedCount++;
        publishedIds.add(validation.normalizedTemplate().id());
      }
    }
    return new TemplateRegistrationResult(descriptors.size(), validCount,
        publishedCount, descriptors.size() - validCount, publishedIds, errors);
  }

  private boolean publish(RawTemplateDescriptor descriptor, DocumentTemplate template) {
    return templateCatalog.publish(template, descriptor.sourceReference(),
        descriptor.sourceType(), true);
  }
}
