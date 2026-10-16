package com.pichincha.cdemsaspopenaidocumentation.documentation.template.port;

import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.DocumentTemplate;
import java.util.List;
import java.util.Optional;

public interface TemplateRepository {

  Optional<DocumentTemplate> resolveActive(String templateId);

  List<String> activeTemplateIds();
}
