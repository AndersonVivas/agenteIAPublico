package com.pichincha.cdemsaspopenaidocumentation.documentation.rendering;

import com.pichincha.cdemsaspopenaidocumentation.documentation.audience.domain.AudienceProfile;
import com.pichincha.cdemsaspopenaidocumentation.documentation.communication.domain.CommunicationProfile;
import com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.domain.RenderingPlan;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.DocumentTemplate;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.ProjectKnowledgeBase;

public interface TemplateRenderer {

  RenderingPlan render(ProjectKnowledgeBase knowledgeBase,
      DocumentTemplate template,
      AudienceProfile audienceProfile,
      CommunicationProfile communicationProfile);
}
