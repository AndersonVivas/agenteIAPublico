package com.pichincha.cdemsaspopenaidocumentation.activity;

import com.pichincha.cdemsaspopenaidocumentation.domain.DocumentGenerationResult;
import com.pichincha.cdemsaspopenaidocumentation.domain.DocumentationGenerationRequest;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface GenerateDocumentationActivity {

  @ActivityMethod
  DocumentGenerationResult generateDocumentation(DocumentationGenerationRequest request);
}
