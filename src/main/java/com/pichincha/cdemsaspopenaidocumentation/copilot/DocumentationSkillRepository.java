package com.pichincha.cdemsaspopenaidocumentation.copilot;

import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentationSkill;
import java.util.List;

/**
 * Loads reusable writing skills (a {@code SKILL.md} file per skill, in the Agent Skills
 * format) that templates declare to shape how the documentation is written.
 */
public interface DocumentationSkillRepository {

  List<DocumentationSkill> findAll(List<String> skillNames);
}
