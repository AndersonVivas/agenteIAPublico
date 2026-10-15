package com.pichincha.cdemsaspopenaidocumentation.documentation.template.service;

import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateResolutionResult;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateVersion;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.port.TemplateRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class CatalogBackedTemplateRepository implements TemplateRepository {

  private final TemplateCatalog templateCatalog;

  public CatalogBackedTemplateRepository(TemplateCatalog templateCatalog) {
    this.templateCatalog = templateCatalog;
  }

  @Override
  public Optional<TemplateResolutionResult> resolveActive(String templateId) {
    return templateCatalog.resolveActive(templateId);
  }

  @Override
  public Optional<TemplateResolutionResult> resolveVersion(String templateId,
      TemplateVersion version) {
    return templateCatalog.resolveVersion(templateId, version);
  }

  @Override
  public List<TemplateResolutionResult> searchByName(String templateName) {
    return templateCatalog.searchByName(templateName);
  }
}
