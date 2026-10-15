package com.pichincha.cdemsaspopenaidocumentation.documentation.template.port;

import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateSourceType;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.service.CatalogBackedTemplateRepository;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.service.TemplateCatalog;

class CatalogBackedTemplateRepositoryContractTest extends TemplateRepositoryContract {

  @Override
  protected TemplateRepository repository() {
    TemplateCatalog catalog = new TemplateCatalog();
    catalog.publish(template("error-manual", "Error Manual", "1.0.0"),
        "local", TemplateSourceType.LOCAL, true);
    catalog.publish(template("error-manual", "Error Manual", "1.1.0"),
        "local", TemplateSourceType.LOCAL, true);
    return new CatalogBackedTemplateRepository(catalog);
  }
}
