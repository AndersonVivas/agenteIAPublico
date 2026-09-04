package com.pichincha.cdemsaspopenaidocumentation.prompt.strategy;

import com.pichincha.cdemsaspopenaidocumentation.prompt.PromptTemplateStrategy;
import com.pichincha.cdemsaspopenaidocumentation.prompt.domain.PromptTemplateDefinition;
import com.pichincha.cdemsaspopenaidocumentation.prompt.domain.PromptType;
import org.springframework.stereotype.Component;

@Component
public class ErrorManualPromptTemplateStrategy implements PromptTemplateStrategy {

  @Override
  public PromptType supportedType() {
    return PromptType.ERROR_MANUAL;
  }

  @Override
  public PromptTemplateDefinition definition() {
    return new PromptTemplateDefinition("error-manual.md", "error-manual-rules.md", "Markdown");
  }
}