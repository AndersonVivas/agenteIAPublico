package com.pichincha.cdemsaspopenaidocumentation.prompt.strategy;

import com.pichincha.cdemsaspopenaidocumentation.prompt.PromptTemplateStrategy;
import com.pichincha.cdemsaspopenaidocumentation.prompt.domain.PromptTemplateDefinition;
import com.pichincha.cdemsaspopenaidocumentation.prompt.domain.PromptType;
import org.springframework.stereotype.Component;

@Component
public class ReadmeGenerationPromptTemplateStrategy implements PromptTemplateStrategy {

  @Override
  public PromptType supportedType() {
    return PromptType.README_GENERATION;
  }

  @Override
  public PromptTemplateDefinition definition() {
    return new PromptTemplateDefinition("readme-generation.md",
        "readme-generation-rules.md", "Markdown");
  }
}