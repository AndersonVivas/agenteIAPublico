package com.pichincha.cdemsaspopenaidocumentation.copilot.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pichincha.cdemsaspopenaidocumentation.copilot.exception.AgentExecutionException;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class SectionBodyResponseParserTest {

  @Test
  void parseShouldKeepOnlyNonBlankBodiesOfRequestedSections() {
    Map<String, String> bodies = SectionBodyResponseParser.parse(
        "{\"summary\": \" Text \", \"extra\": \"Ignored\", \"glossary\": \" \"}",
        Set.of("summary", "glossary"), "render");

    assertEquals(Map.of("summary", "Text"), bodies);
  }

  @Test
  void parseShouldFailWithTheAgentLabelWhenTheResponseIsNotJson() {
    AgentExecutionException exception = assertThrows(AgentExecutionException.class,
        () -> SectionBodyResponseParser.parse("not json", Set.of("summary"), "editorial"));

    assertTrue(exception.getMessage().contains("editorial"));
  }
}
