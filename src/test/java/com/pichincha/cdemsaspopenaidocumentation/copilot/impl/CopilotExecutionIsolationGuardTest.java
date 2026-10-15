package com.pichincha.cdemsaspopenaidocumentation.copilot.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class CopilotExecutionIsolationGuardTest {

  private static final Path MAIN_SOURCE_ROOT = Path.of("src/main/java");
  private static final Path EXECUTOR_SOURCE = Path.of("src/main/java/com/pichincha"
      + "/cdemsaspopenaidocumentation/copilot/impl/CopilotCliPromptExecutor.java");
  private static final Path SERVICE_SOURCE = Path.of("src/main/java/com/pichincha"
      + "/cdemsaspopenaidocumentation/copilot/impl/DefaultPromptExecutionService.java");
  private static final Path CONTROLLER_SOURCE = Path.of("src/main/java/com/pichincha"
      + "/cdemsaspopenaidocumentation/controller/PromptExecutionController.java");
  private static final Path CONTROLLERS_ROOT = Path.of("src/main/java/com/pichincha"
      + "/cdemsaspopenaidocumentation/controller");
  private static final Pattern FORBIDDEN_REPOSITORY_CWD = Pattern.compile(
      "builder\\s*\\.\\s*directory\\s*\\(\\s*repositoryPath\\s*\\.\\s*toFile\\s*\\(");
  private static final Pattern FORBIDDEN_REPOSITORY_EXECUTION = Pattern.compile(
      "promptCommandExecutor\\s*\\.\\s*execute\\s*\\([^;]*repositoryPath",
      Pattern.DOTALL);

  @Test
  void shouldNeverUseRepositoryPathAsCopilotWorkingDirectory() throws IOException {
    String executor = Files.readString(EXECUTOR_SOURCE);

    assertFalse(FORBIDDEN_REPOSITORY_CWD.matcher(executor).find());
    assertFalse(executor.contains("repositoryPath"));
  }

  @Test
  void shouldFailIfRepositoryPathIsEverUsedAsCwdInMainSource() throws IOException {
    List<Path> javaFiles;
    try (var walk = Files.walk(MAIN_SOURCE_ROOT)) {
      javaFiles = walk.filter(path -> path.toString().endsWith(".java")).toList();
    }
    for (Path javaFile : javaFiles) {
      String source = Files.readString(javaFile);
      assertFalse(FORBIDDEN_REPOSITORY_CWD.matcher(source).find());
    }
  }

  @Test
  void shouldNeverPassRepositoryPathToCopilotExecution() throws IOException {
    String service = Files.readString(SERVICE_SOURCE);

    assertFalse(FORBIDDEN_REPOSITORY_EXECUTION.matcher(service).find());
  }

  @Test
  void shouldNeverExposePromptExecutionRequestInPublicController() throws IOException {
    String controller = Files.readString(CONTROLLER_SOURCE);

    assertFalse(controller.contains("PromptExecutionRequest"));
  }

  @Test
  void shouldNeverExposePromptContentOrRepositoryPathInPublicControllers()
      throws IOException {
    List<Path> javaFiles;
    try (var walk = Files.walk(CONTROLLERS_ROOT)) {
      javaFiles = walk.filter(path -> path.toString().endsWith(".java")).toList();
    }
    for (Path javaFile : javaFiles) {
      String source = Files.readString(javaFile);
      assertFalse(source.contains("promptContent"));
      assertFalse(source.contains("repositoryPath"));
    }
  }
}
