package com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.pichincha.cdemsaspopenaidocumentation.documentation.communication.domain.PresentationMode;
import com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.domain.SectionContext;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.EvidenceRef;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class TraceabilityAssemblerTest {

  private final TraceabilityAssembler assembler = new TraceabilityAssembler();

  @Test
  void shouldCreateTraceabilityIndexBySection() {
    SectionContext section = new SectionContext("api", "API", "status", true, 1,
        "intent", PresentationMode.SUMMARY_FIRST, List.of("a"),
        List.of(new EvidenceRef("e2", "f", "r", "s", 0.8),
            new EvidenceRef("e1", "f", "r", "s", 0.8),
            new EvidenceRef("e1", "f", "r", "s", 0.8)),
        List.of(), "");

    Map<String, List<String>> index = assembler.assemble(List.of(section));

    assertEquals(List.of("e1", "e2"), index.get("api"));
  }
}
