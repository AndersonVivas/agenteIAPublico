package com.pichincha.cdemsaspopenaidocumentation.copilot.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentStateSnapshot;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.RenderedSections;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.SectionState;
import com.pichincha.cdemsaspopenaidocumentation.documentation.communication.domain.PresentationMode;
import com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.domain.SectionContext;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.EvidenceRef;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class IncrementalSectionPlannerTest {

  private final IncrementalSectionPlanner planner = new IncrementalSectionPlanner();

  @Test
  void impactedSectionsShouldReturnOnlySectionsWhoseInputsChanged() {
    SectionContext unchanged = section("scope", "EV-1", List.of());
    SectionContext changed = section("catalog", "EV-2", List.of());
    DocumentStateSnapshot previous = new DocumentStateSnapshot(Map.of(
        "scope", state(planner.fingerprint(unchanged, "sig")),
        "catalog", state(planner.fingerprint(section("catalog", "EV-OLD", List.of()), "sig"))));

    List<SectionContext> impacted = planner.impactedSections(List.of(unchanged, changed),
        previous, "sig");

    assertEquals(List.of(changed), impacted);
  }

  @Test
  void fingerprintShouldChangeWithInstructionsAndRenderSignature() {
    SectionContext section = section("scope", "EV-1", List.of());

    assertNotEquals(planner.fingerprint(section, "sig"),
        planner.fingerprint(section("scope", "EV-1", List.of("New rule")), "sig"));
    assertNotEquals(planner.fingerprint(section, "sig"), planner.fingerprint(section, "sig-2"));
  }

  @Test
  void renderedStatesShouldOnlyIncludeSectionsReturnedByTheRenderAgent() {
    SectionContext rendered = section("scope", "EV-1", List.of());
    SectionContext omitted = section("catalog", "EV-2", List.of());

    Map<String, SectionState> states = planner.renderedStates(List.of(rendered, omitted),
        new RenderedSections(Map.of("scope", "body"), List.of()), "sig", 3);

    assertEquals(List.of("scope"), List.copyOf(states.keySet()));
    assertEquals("body", states.get("scope").body());
    assertEquals(3, states.get("scope").documentVersion());
  }

  private SectionContext section(String id, String evidenceId, List<String> instructions) {
    return new SectionContext(id, id, "objective", instructions, true, 1, "intent",
        PresentationMode.SUMMARY_FIRST, List.of("highlight"),
        List.of(new EvidenceRef(evidenceId, "A.java", "rule", "api", 1.0)), List.of(), "");
  }

  private SectionState state(String fingerprint) {
    return new SectionState("title", fingerprint, "body", 1);
  }
}
