package com.pichincha.cdemsaspopenaidocumentation.copilot.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.RenderedSections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class EditorialRevisionMergerTest {

  private final RenderedSections drafts = new RenderedSections(Map.of(
      "catalog", "Timeout in payments (EV-abc123).",
      "summary", "The service registers payments."), List.of("draft warning"));

  @Test
  void mergeShouldAcceptRevisionsThatKeepTheSameEvidenceIds() {
    RenderedSections merged = EditorialRevisionMerger.merge(drafts, Map.of(
        "catalog", "Payments take too long to answer (EV-abc123).",
        "summary", "This service records customer payments."));

    assertEquals("Payments take too long to answer (EV-abc123).", merged.bodyOf("catalog"));
    assertEquals("This service records customer payments.", merged.bodyOf("summary"));
    assertEquals(List.of("draft warning"), merged.warnings());
  }

  @Test
  void mergeShouldKeepTheDraftWhenTheRevisionChangesTheEvidenceIds() {
    RenderedSections merged = EditorialRevisionMerger.merge(drafts, Map.of(
        "catalog", "Payments take too long to answer (EV-def456).",
        "summary", "This service records customer payments (EV-abc123)."));

    assertEquals("Timeout in payments (EV-abc123).", merged.bodyOf("catalog"));
    assertEquals("The service registers payments.", merged.bodyOf("summary"));
    assertEquals(3, merged.warnings().size());
    assertTrue(merged.warnings().get(1).contains("evidence ids"));
  }

  @Test
  void mergeShouldKeepDraftsThatWereNotRevised() {
    RenderedSections merged = EditorialRevisionMerger.merge(drafts, Map.of());

    assertEquals(drafts.bodiesBySectionId(), merged.bodiesBySectionId());
  }
}
