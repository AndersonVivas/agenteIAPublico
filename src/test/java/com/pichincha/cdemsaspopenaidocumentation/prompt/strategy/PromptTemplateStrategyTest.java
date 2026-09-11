package com.pichincha.cdemsaspopenaidocumentation.prompt.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.pichincha.cdemsaspopenaidocumentation.prompt.PromptTemplateStrategy;
import com.pichincha.cdemsaspopenaidocumentation.prompt.domain.PromptTemplateDefinition;
import com.pichincha.cdemsaspopenaidocumentation.prompt.domain.PromptType;
import org.junit.jupiter.api.Test;

class PromptTemplateStrategyTest {

  @Test
  void changelogStrategyShouldProvideValidDefinition() {
    PromptTemplateStrategy strategy = new ChangelogPromptTemplateStrategy();
    assertEquals(PromptType.CHANGELOG, strategy.supportedType());
    assertStrategyDefinition(strategy, "changelog.md", "changelog-rules.md");
  }

  @Test
  void codeDocumentationStrategyShouldProvideValidDefinition() {
    PromptTemplateStrategy strategy = new CodeDocumentationPromptTemplateStrategy();
    assertEquals(PromptType.CODE_DOCUMENTATION, strategy.supportedType());
    assertStrategyDefinition(strategy, "code-documentation.md", "code-documentation-rules.md");
  }

  @Test
  void technicalSpecificationStrategyShouldProvideValidDefinition() {
    PromptTemplateStrategy strategy = new TechnicalSpecificationPromptTemplateStrategy();
    assertEquals(PromptType.TECHNICAL_SPECIFICATION, strategy.supportedType());
    assertStrategyDefinition(
        strategy, "technical-specification.md", "technical-specification-rules.md");
  }

  @Test
  void deploymentGuideStrategyShouldProvideValidDefinition() {
    PromptTemplateStrategy strategy = new DeploymentGuidePromptTemplateStrategy();
    assertEquals(PromptType.DEPLOYMENT_GUIDE, strategy.supportedType());
    assertStrategyDefinition(strategy, "deployment-guide.md", "deployment-guide-rules.md");
  }

  private void assertStrategyDefinition(PromptTemplateStrategy strategy,
      String expectedTemplate, String expectedRules) {
    PromptTemplateDefinition definition = strategy.definition();
    assertNotNull(definition);
    assertEquals(expectedTemplate, definition.templateResource());
    assertEquals(expectedRules, definition.rulesResource());
    assertEquals("Markdown", definition.outputFormat());
  }
}
