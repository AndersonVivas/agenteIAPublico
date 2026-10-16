package com.pichincha.cdemsaspopenaidocumentation.copilot.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.pichincha.cdemsaspopenaidocumentation.copilot.DocumentationSkillRepository;
import com.pichincha.cdemsaspopenaidocumentation.copilot.config.DocumentationSkillsProperties;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentationSkill;
import com.pichincha.cdemsaspopenaidocumentation.copilot.exception.SkillNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Reads {@code <skills path>/<name>/SKILL.md}: YAML front matter with {@code name} and
 * {@code description}, followed by the writing instructions.
 */
@Component
@RequiredArgsConstructor
public class FileSystemDocumentationSkillRepository implements DocumentationSkillRepository {

  private static final String SKILL_FILE = "SKILL.md";
  private static final Pattern FRONT_MATTER = Pattern.compile(
      "\\A---\\R(.*?)\\R---\\R?(.*)\\z", Pattern.DOTALL);
  private static final ObjectMapper YAML_MAPPER = new ObjectMapper(new YAMLFactory());

  private final DocumentationSkillsProperties properties;

  @Override
  public List<DocumentationSkill> findAll(List<String> skillNames) {
    return skillNames.stream().map(this::load).toList();
  }

  private DocumentationSkill load(String skillName) {
    Path skillFile = properties.path().resolve(skillName).resolve(SKILL_FILE);
    Matcher matcher = FRONT_MATTER.matcher(read(skillName, skillFile));
    if (!matcher.matches()) {
      throw new SkillNotFoundException(skillName, "missing YAML front matter in " + skillFile);
    }
    JsonNode frontMatter = parse(skillName, matcher.group(1));
    String instructions = matcher.group(2).strip();
    if (!skillName.equals(frontMatter.path("name").asText())
        || frontMatter.path("description").asText().isBlank() || instructions.isEmpty()) {
      throw new SkillNotFoundException(skillName,
          "front matter must declare the same name, a description and instructions");
    }
    return new DocumentationSkill(skillName, frontMatter.path("description").asText().strip(),
        instructions);
  }

  private String read(String skillName, Path skillFile) {
    try {
      return Files.readString(skillFile, StandardCharsets.UTF_8);
    } catch (IOException exception) {
      throw new SkillNotFoundException(skillName, "unable to read " + skillFile, exception);
    }
  }

  private JsonNode parse(String skillName, String frontMatter) {
    try {
      return YAML_MAPPER.readTree(frontMatter);
    } catch (IOException exception) {
      throw new SkillNotFoundException(skillName, "invalid YAML front matter", exception);
    }
  }
}
