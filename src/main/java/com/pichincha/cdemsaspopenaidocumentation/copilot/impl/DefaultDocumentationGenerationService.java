package com.pichincha.cdemsaspopenaidocumentation.copilot.impl;

import com.pichincha.cdemsaspopenaidocumentation.copilot.DocumentationContextRepository;
import com.pichincha.cdemsaspopenaidocumentation.copilot.DocumentationGenerationService;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentationGenerationContext;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.DocumentTemplate;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.service.DocumentTemplateService;
import com.pichincha.cdemsaspopenaidocumentation.domain.DocumentGenerationResult;
import com.pichincha.cdemsaspopenaidocumentation.domain.DocumentationGenerationRequest;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.ProjectKnowledgeBase;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.service.KnowledgeBaseAggregator;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.service.KnowledgeProjectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DefaultDocumentationGenerationService implements DocumentationGenerationService {

  private final DocumentTemplateService documentTemplateService;
  private final DocumentationContextRepository documentationContextRepository;
  private final KnowledgeProjectionService knowledgeProjectionService;
  private final KnowledgeBaseAggregator knowledgeBaseAggregator;
  private final DocumentContextFactory documentContextFactory;
  private final MultiAgentDocumentationOrchestrator orchestrator;

  @Override
  public DocumentGenerationResult generate(DocumentationGenerationRequest request) {
    DocumentTemplate template = documentTemplateService.requireTemplate(request.templateId());
    DocumentationGenerationContext context = documentationContextRepository.load(
        request.scanId(), template.guidance().lifecycle());
    ProjectKnowledgeBase knowledgeBase = knowledgeBaseAggregator.aggregate(
        knowledgeProjectionService.project(context.evidences()));
    return orchestrator.generate(documentContextFactory.create(context, knowledgeBase,
        template));
  }
}
