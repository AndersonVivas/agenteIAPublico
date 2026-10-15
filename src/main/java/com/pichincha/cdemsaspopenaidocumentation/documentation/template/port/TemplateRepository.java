package com.pichincha.cdemsaspopenaidocumentation.documentation.template.port;

import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateResolutionResult;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateVersion;
import java.util.List;
import java.util.Optional;

public interface TemplateRepository {

  Optional<TemplateResolutionResult> resolveActive(String templateId);

  Optional<TemplateResolutionResult> resolveVersion(String templateId,
      TemplateVersion version);

  List<TemplateResolutionResult> searchByName(String templateName);
}
