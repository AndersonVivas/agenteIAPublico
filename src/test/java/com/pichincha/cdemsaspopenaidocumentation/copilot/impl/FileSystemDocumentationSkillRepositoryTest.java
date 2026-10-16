package com.pichincha.cdemsaspopenaidocumentation.copilot.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.pichincha.cdemsaspopenaidocumentation.copilot.config.DocumentationSkillsProperties;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentationSkill;
import com.pichincha.cdemsaspopenaidocumentation.copilot.exception.SkillNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileSystemDocumentationSkillRepositoryTest {

  @TempDir
  private Path skillsHome;

  @Test
  void findAllShouldLoadSkillsInDeclaredOrder() throws IOException {
    writeSkill("glosario", skill("glosario", "Explain terms."));
    writeSkill("lenguaje-claro", skill("lenguaje-claro", "Use short sentences."));

    List<DocumentationSkill> skills = repository().findAll(List.of("lenguaje-claro",
        "glosario"));

    assertEquals(List.of("lenguaje-claro", "glosario"),
        skills.stream().map(DocumentationSkill::name).toList());
    assertEquals("Writing rules", skills.getFirst().description());
    assertEquals("Use short sentences.", skills.getFirst().instructions());
  }

  @Test
  void findAllShouldRejectMissingSkill() {
    assertThrows(SkillNotFoundException.class,
        () -> repository().findAll(List.of("missing")));
  }

  @Test
  void findAllShouldRejectSkillWhoseNameDoesNotMatchItsFolder() throws IOException {
    writeSkill("glosario", skill("other-name", "Explain terms."));

    assertThrows(SkillNotFoundException.class,
        () -> repository().findAll(List.of("glosario")));
  }

  @Test
  void findAllShouldRejectSkillWithoutFrontMatter() throws IOException {
    writeSkill("glosario", "Explain terms.");

    assertThrows(SkillNotFoundException.class,
        () -> repository().findAll(List.of("glosario")));
  }

  private FileSystemDocumentationSkillRepository repository() {
    return new FileSystemDocumentationSkillRepository(
        new DocumentationSkillsProperties(skillsHome));
  }

  private String skill(String name, String instructions) {
    return "---\nname: " + name + "\ndescription: Writing rules\n---\n\n" + instructions + "\n";
  }

  private void writeSkill(String folder, String content) throws IOException {
    Path directory = Files.createDirectories(skillsHome.resolve(folder));
    Files.writeString(directory.resolve("SKILL.md"), content);
  }
}
