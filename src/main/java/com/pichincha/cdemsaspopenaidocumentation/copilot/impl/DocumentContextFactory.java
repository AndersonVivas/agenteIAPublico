package com.pichincha.cdemsaspopenaidocumentation.copilot.impl;

import com.pichincha.cdemsaspopenaidocumentation.copilot.DocumentationSkillRepository;
import com.pichincha.cdemsaspopenaidocumentation.copilot.EditorialReviewAgent;
import com.pichincha.cdemsaspopenaidocumentation.copilot.TemplateRenderAgent;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentContext;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentationGenerationContext;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentationSkill;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.OutputContract;
import com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.TemplateRenderer;
import com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.domain.RenderingPlan;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.DocumentTemplate;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateGuidance;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.ProjectKnowledgeBase;
import com.pichincha.cdemsaspopenaidocumentation.util.HashUtil;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DocumentContextFactory {

  private final TemplateRenderer templateRenderer;
  private final TemplateRenderAgent templateRenderAgent;
  private final EditorialReviewAgent editorialReviewAgent;
  private final DocumentationSkillRepository skillRepository;

  public DocumentContext create(DocumentationGenerationContext generationContext,
      ProjectKnowledgeBase knowledgeBase, DocumentTemplate template) {
    RenderingPlan renderingPlan = templateRenderer.render(knowledgeBase, template);
    TemplateGuidance guidance = template.guidance();
    List<DocumentationSkill> skills = skillRepository.findAll(guidance.skills());
    OutputContract outputContract = new OutputContract(template.reader().language(),
        guidance.missingDataMessage(), guidance.globalRules());
    return new DocumentContext(generationContext, template, outputContract, renderingPlan,
        skills, renderSignature(template, skills));
  }

  private String renderSignature(DocumentTemplate template, List<DocumentationSkill> skills) {
    String skillContent = skills.stream()
        .map(skill -> skill.name() + ":" + skill.instructions())
        .collect(Collectors.joining("|"));
    String editorialSignature = template.guidance().editorialReview()
        ? editorialReviewAgent.reviewSignature() : "";
    return HashUtil.sha256Hex(String.join("|", template.metadata().checksum(),
        templateRenderAgent.renderSignature(), skillContent, editorialSignature));
  }
}
