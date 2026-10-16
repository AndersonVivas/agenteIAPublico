package com.pichincha.cdemsaspopenaidocumentation.copilot.support;

import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentationSkill;
import java.util.List;
import java.util.stream.Collectors;

public final class SkillPromptFormatter {

  private static final String NO_SKILLS = "- Sin skills de redaccion adicionales.";

  private SkillPromptFormatter() {
  }

  public static String format(List<DocumentationSkill> skills) {
    if (skills.isEmpty()) {
      return NO_SKILLS;
    }
    return skills.stream()
        .map(skill -> "### Skill: " + skill.name() + "\n" + skill.description() + "\n\n"
            + skill.instructions())
        .collect(Collectors.joining("\n\n"));
  }
}
