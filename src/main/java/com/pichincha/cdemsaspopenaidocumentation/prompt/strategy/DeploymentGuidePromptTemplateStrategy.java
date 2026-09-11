package com.pichincha.cdemsaspopenaidocumentation.prompt.strategy;

import com.pichincha.cdemsaspopenaidocumentation.prompt.PromptTemplateStrategy;
import com.pichincha.cdemsaspopenaidocumentation.prompt.domain.PromptTemplateDefinition;
import com.pichincha.cdemsaspopenaidocumentation.prompt.domain.PromptType;
import org.springframework.stereotype.Component;

@Component
public class DeploymentGuidePromptTemplateStrategy implements PromptTemplateStrategy {

  @Override
  public PromptType supportedType() {
    return PromptType.DEPLOYMENT_GUIDE;
  }

  @Override
  public PromptTemplateDefinition definition() {
    return new PromptTemplateDefinition(
        "deployment-guide.md",
        "deployment-guide-rules.md",
        "Markdown");
  }
}
