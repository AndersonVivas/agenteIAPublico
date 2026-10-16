package com.pichincha.cdemsaspopenaidocumentation.documentation.template.port;

import com.pichincha.cdemsaspopenaidocumentation.documentation.template.service.CatalogBackedTemplateRepository;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.service.TemplateCatalog;
import java.util.List;

class CatalogBackedTemplateRepositoryContractTest extends TemplateRepositoryContract {

  @Override
  protected TemplateRepository repository() {
    TemplateCatalog catalog = new TemplateCatalog();
    catalog.replaceAll(List.of(template("error-manual", "Error Manual", "1.0.0"),
        template("error-manual", "Error Manual", "1.1.0")));
    return new CatalogBackedTemplateRepository(catalog);
  }
}
