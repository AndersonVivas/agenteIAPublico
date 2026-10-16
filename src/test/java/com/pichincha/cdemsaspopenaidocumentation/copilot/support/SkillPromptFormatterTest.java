package com.pichincha.cdemsaspopenaidocumentation.copilot.support;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentationSkill;
import java.util.List;
import org.junit.jupiter.api.Test;

class SkillPromptFormatterTest {

  @Test
  void formatShouldRenderEachSkillWithNameDescriptionAndInstructions() {
    String prompt = SkillPromptFormatter.format(List.of(
        new DocumentationSkill("glosario", "Explain terms", "Add a glossary."),
        new DocumentationSkill("lenguaje-claro", "Plain language", "Use short sentences.")));

    assertEquals("""
        ### Skill: glosario
        Explain terms

        Add a glossary.

        ### Skill: lenguaje-claro
        Plain language

        Use short sentences.""", prompt);
  }

  @Test
  void formatShouldStateThatThereAreNoSkills() {
    assertEquals("- Sin skills de redaccion adicionales.", SkillPromptFormatter.format(List.of()));
  }
}
