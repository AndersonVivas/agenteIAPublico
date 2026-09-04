package com.pichincha.cdemsaspopenaidocumentation.prompt.strategy;

import com.pichincha.cdemsaspopenaidocumentation.prompt.PromptTemplateStrategy;
import com.pichincha.cdemsaspopenaidocumentation.prompt.domain.PromptTemplateDefinition;
import com.pichincha.cdemsaspopenaidocumentation.prompt.domain.PromptType;
import org.springframework.stereotype.Component;

@Component
public class ApiDocumentationPromptTemplateStrategy implements PromptTemplateStrategy {

  @Override
  public PromptType supportedType() {
    return PromptType.API_DOCUMENTATION;
  }

  @Override
  public PromptTemplateDefinition definition() {
    return new PromptTemplateDefinition("api-documentation.md",
        "api-documentation-rules.md", "Markdown");
  }
}