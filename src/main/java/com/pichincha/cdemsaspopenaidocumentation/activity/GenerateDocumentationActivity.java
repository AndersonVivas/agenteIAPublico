package com.pichincha.cdemsaspopenaidocumentation.activity;

import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentationGenerationContext;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.PromptExecutionResult;
import io.temporal.activity.ActivityMethod;

public interface GenerateDocumentationActivity {

  @ActivityMethod
  PromptExecutionResult generateDocumentation(DocumentationGenerationContext context,
      String templateId);
}
