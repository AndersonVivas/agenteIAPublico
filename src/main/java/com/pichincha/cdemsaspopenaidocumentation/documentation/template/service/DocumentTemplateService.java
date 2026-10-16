package com.pichincha.cdemsaspopenaidocumentation.documentation.template.service;

import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.DocumentTemplate;
import java.util.List;

public interface DocumentTemplateService {

  /**
   * Reloads the template directory and returns the requested template identifiers, or every
   * active template when none is requested. Fails when a requested template does not exist.
   */
  List<String> resolveTemplateIds(List<String> requestedTemplateIds);

  /**
   * Reloads the template directory and returns the active template with the given identifier.
   */
  DocumentTemplate requireTemplate(String templateId);
}
