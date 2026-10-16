package com.pichincha.cdemsaspopenaidocumentation.documentation.template.service;

import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.DocumentTemplate;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.port.TemplateRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CatalogBackedTemplateRepository implements TemplateRepository {

  private final TemplateCatalog templateCatalog;

  @Override
  public Optional<DocumentTemplate> resolveActive(String templateId) {
    return templateCatalog.resolveActive(templateId);
  }

  @Override
  public List<String> activeTemplateIds() {
    return templateCatalog.activeTemplateIds();
  }
}
