package com.pichincha.cdemsaspopenaidocumentation.copilot;

import com.pichincha.cdemsaspopenaidocumentation.domain.DocumentGenerationResult;
import com.pichincha.cdemsaspopenaidocumentation.domain.DocumentationGenerationRequest;

public interface DocumentationGenerationService {

  /**
   * Generates or updates the document of one template from a stored scan, regenerating only
   * the sections impacted by the scan according to the template lifecycle.
   */
  DocumentGenerationResult generate(DocumentationGenerationRequest request);
}
