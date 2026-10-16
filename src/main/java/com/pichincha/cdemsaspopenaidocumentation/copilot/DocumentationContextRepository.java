package com.pichincha.cdemsaspopenaidocumentation.copilot;

import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentationGenerationContext;
import com.pichincha.cdemsaspopenaidocumentation.domain.enums.DocumentLifecycleEnum;

/**
 * Loads the stored result of a scan as a generation context. The evidence scope depends on the
 * document lifecycle: the current repository snapshot for evolutionary documents, or only the
 * evidence that changed in the scan for historical documents.
 */
public interface DocumentationContextRepository {

  DocumentationGenerationContext load(String scanId, DocumentLifecycleEnum lifecycle);
}
