package com.pichincha.cdemsaspopenaidocumentation.prompt;

import com.pichincha.cdemsaspopenaidocumentation.prompt.domain.PromptTemplateDefinition;
import com.pichincha.cdemsaspopenaidocumentation.prompt.domain.PromptType;

public interface PromptTemplateStrategy {

  PromptType supportedType();

  PromptTemplateDefinition definition();
}