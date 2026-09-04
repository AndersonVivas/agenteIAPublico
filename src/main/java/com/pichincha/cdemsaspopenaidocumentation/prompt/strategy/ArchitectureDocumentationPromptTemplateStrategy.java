package com.pichincha.cdemsaspopenaidocumentation.prompt.strategy;

import com.pichincha.cdemsaspopenaidocumentation.prompt.PromptTemplateStrategy;
import com.pichincha.cdemsaspopenaidocumentation.prompt.domain.PromptTemplateDefinition;
import com.pichincha.cdemsaspopenaidocumentation.prompt.domain.PromptType;
import org.springframework.stereotype.Component;

@Component
public class ArchitectureDocumentationPromptTemplateStrategy implements PromptTemplateStrategy {

  @Override
  public PromptType supportedType() {
    return PromptType.ARCHITECTURE_DOCUMENTATION;
  }

  @Override
  public PromptTemplateDefinition definition() {
    return new PromptTemplateDefinition("architecture-documentation.md",
        "architecture-documentation-rules.md", "Markdown");
  }
}