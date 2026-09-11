package com.pichincha.cdemsaspopenaidocumentation.prompt.strategy;

import com.pichincha.cdemsaspopenaidocumentation.prompt.PromptTemplateStrategy;
import com.pichincha.cdemsaspopenaidocumentation.prompt.domain.PromptTemplateDefinition;
import com.pichincha.cdemsaspopenaidocumentation.prompt.domain.PromptType;
import org.springframework.stereotype.Component;

@Component
public class CodeDocumentationPromptTemplateStrategy implements PromptTemplateStrategy {

  @Override
  public PromptType supportedType() {
    return PromptType.CODE_DOCUMENTATION;
  }

  @Override
  public PromptTemplateDefinition definition() {
    return new PromptTemplateDefinition(
        "code-documentation.md",
        "code-documentation-rules.md",
        "Markdown");
  }
}
