package com.pichincha.cdemsaspopenaidocumentation.prompt.strategy;

import com.pichincha.cdemsaspopenaidocumentation.prompt.PromptTemplateStrategy;
import com.pichincha.cdemsaspopenaidocumentation.prompt.domain.PromptTemplateDefinition;
import com.pichincha.cdemsaspopenaidocumentation.prompt.domain.PromptType;
import org.springframework.stereotype.Component;

@Component
public class TechnicalSpecificationPromptTemplateStrategy implements PromptTemplateStrategy {

  @Override
  public PromptType supportedType() {
    return PromptType.TECHNICAL_SPECIFICATION;
  }

  @Override
  public PromptTemplateDefinition definition() {
    return new PromptTemplateDefinition(
        "technical-specification.md",
        "technical-specification-rules.md",
        "Markdown");
  }
}
