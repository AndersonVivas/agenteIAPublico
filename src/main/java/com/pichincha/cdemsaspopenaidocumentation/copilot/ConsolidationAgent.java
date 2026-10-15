package com.pichincha.cdemsaspopenaidocumentation.copilot;

import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.ConsolidatedKnowledge;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.ConsolidationRequest;

/**
 * Merges structured knowledge produced by the specialist agents into a single,
 * deduplicated, conflict-aware view. Still produces structured knowledge, not
 * final documentation text. Temporal-Activity-ready: plain request/response types.
 */
public interface ConsolidationAgent {

  ConsolidatedKnowledge consolidate(ConsolidationRequest request);
}
