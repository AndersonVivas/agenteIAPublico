package com.pichincha.cdemsaspopenaidocumentation.copilot.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pichincha.cdemsaspopenaidocumentation.domain.CodeSignalEvidence;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

class AgentEvidenceFormatterTest {

  @Test
  void formatShouldIncludeSurroundingCodeLinesExceptTheMatchedLine() {
    String prompt = AgentEvidenceFormatter.format(List.of(evidence("EV-1", List.of(
        "10: if (customer == null) {",
        "11:   throw new CustomerNotFoundException(id);",
        "12: }",
        "13:"))));

    assertTrue(prompt.contains("[id:EV-1]"));
    assertTrue(prompt.contains("texto: throw new CustomerNotFoundException(id);"));
    assertTrue(prompt.contains("contexto:\n     10: if (customer == null) {\n     12: }"));
    assertEquals(1, prompt.split("CustomerNotFoundException", -1).length - 1);
    assertFalse(prompt.contains("13:"));
  }

  @Test
  void formatShouldStopAddingContextWhenTheBudgetIsExhausted() {
    String longLine = "x".repeat(150);
    List<CodeSignalEvidence> evidences = IntStream.range(0, 400)
        .mapToObj(index -> evidence("EV-" + index, List.of("10: " + longLine,
            "12: " + longLine)))
        .toList();

    String prompt = AgentEvidenceFormatter.format(evidences);

    assertTrue(prompt.contains("[id:EV-399]"));
    assertTrue(prompt.split("contexto:", -1).length - 1 < 400);
  }

  @Test
  void formatShouldReportMissingEvidence() {
    assertTrue(AgentEvidenceFormatter.format(List.of()).startsWith("- No se encontro"));
  }

  private CodeSignalEvidence evidence(String evidenceId, List<String> snippet) {
    return new CodeSignalEvidence(evidenceId, "scan", "EXCEPTION", "code", "java", "spring",
        "CustomerService.java", "find", "CustomerService", "EXCEPTION_THROW", "THROW",
        "throw new CustomerNotFoundException(id);", "medium", "request", List.of(), "cause",
        "action", 10, 13, 11, snippet, 0.9, List.of());
  }
}
