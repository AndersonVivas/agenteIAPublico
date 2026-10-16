package com.pichincha.cdemsaspopenaidocumentation.copilot;

import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentStateSnapshot;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.SectionState;
import java.util.Map;

/**
 * Persists, per evolutionary document, the fingerprint and body of every generated section so
 * the next execution regenerates only the sections whose inputs changed.
 */
public interface DocumentStateRepository {

  DocumentStateSnapshot load(String documentKey);

  void save(String documentKey, Map<String, SectionState> sectionsById);
}
