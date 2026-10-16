package com.pichincha.cdemsaspopenaidocumentation.copilot;

import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.EditorialReviewRequest;
import java.util.Map;

/**
 * Second writing pass over the sections drafted by the render agent: rewrites them for the
 * reader of the template applying its writing skills, without adding facts or changing the
 * cited evidence.
 */
public interface EditorialReviewAgent {

  /**
   * Returns the revised Markdown body per section id. Sections that are not returned keep
   * their draft.
   */
  Map<String, String> review(EditorialReviewRequest request);

  /**
   * Fingerprint of the editorial prompt and model; changing it regenerates the sections of
   * the templates that use the editorial review.
   */
  String reviewSignature();
}
