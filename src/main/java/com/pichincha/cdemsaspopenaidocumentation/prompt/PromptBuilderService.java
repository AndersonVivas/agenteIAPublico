package com.pichincha.cdemsaspopenaidocumentation.prompt;

import com.pichincha.cdemsaspopenaidocumentation.prompt.domain.PromptContext;
import com.pichincha.cdemsaspopenaidocumentation.prompt.domain.PromptType;

public interface PromptBuilderService {

  String buildPrompt(PromptType promptType, PromptContext context);
}