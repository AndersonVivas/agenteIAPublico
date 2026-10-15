package com.pichincha.cdemsaspopenaidocumentation.copilot.impl;

import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentContext;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentProvenance;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentTemplateType;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentationGenerationContext;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.OutputContract;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.ResolvedTemplateContext;
import com.pichincha.cdemsaspopenaidocumentation.documentation.audience.domain.AudienceOverride;
import com.pichincha.cdemsaspopenaidocumentation.documentation.audience.domain.AudienceProfile;
import com.pichincha.cdemsaspopenaidocumentation.documentation.audience.domain.AudienceType;
import com.pichincha.cdemsaspopenaidocumentation.documentation.audience.domain.ResolvedAudienceProfile;
import com.pichincha.cdemsaspopenaidocumentation.documentation.audience.service.AudienceProfileResolver;
import com.pichincha.cdemsaspopenaidocumentation.documentation.communication.domain.CommunicationProfile;
import com.pichincha.cdemsaspopenaidocumentation.documentation.communication.service.CommunicationProfileResolver;
import com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.TemplateRenderer;
import com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.domain.RenderingPlan;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.DocumentTemplate;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateResolutionResult;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateSection;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.port.TemplateRepository;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.ProjectKnowledgeBase;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class DocumentContextFactory {

  private final TemplateRepository templateRepository;
  private final AudienceProfileResolver audienceProfileResolver;
  private final CommunicationProfileResolver communicationProfileResolver;
  private final TemplateRenderer templateRenderer;

  public DocumentContextFactory(TemplateRepository templateRepository,
      AudienceProfileResolver audienceProfileResolver,
      CommunicationProfileResolver communicationProfileResolver,
      TemplateRenderer templateRenderer) {
    this.templateRepository = templateRepository;
    this.audienceProfileResolver = audienceProfileResolver;
    this.communicationProfileResolver = communicationProfileResolver;
    this.templateRenderer = templateRenderer;
  }

  public DocumentContext create(DocumentationGenerationContext generationContext,
      ProjectKnowledgeBase knowledgeBase, String templateId) {
    return create(generationContext, knowledgeBase, templateId,
        (AudienceOverride) null);
  }

  public DocumentContext create(DocumentationGenerationContext generationContext,
      ProjectKnowledgeBase knowledgeBase, String templateId,
      AudienceType requestedAudience) {
    AudienceOverride audienceOverride = requestedAudience == null ? null
        : new AudienceOverride(requestedAudience, "", "requested-by-adapter", true);
    return create(generationContext, knowledgeBase, templateId, audienceOverride);
  }

  private DocumentContext create(DocumentationGenerationContext generationContext,
      ProjectKnowledgeBase knowledgeBase, String templateId,
      AudienceOverride audienceOverride) {
    TemplateResolutionResult resolution = resolveTemplate(templateId);
    ResolvedAudienceProfile resolvedAudience = audienceProfileResolver
        .resolve(resolution.template(), audienceOverride);
    AudienceProfile audienceProfile = resolvedAudience.profile();
    CommunicationProfile communicationProfile = communicationProfileResolver
        .resolve(resolution.template(), audienceProfile);
    RenderingPlan renderingPlan = templateRenderer.render(knowledgeBase,
        resolution.template(), audienceProfile, communicationProfile);
    ResolvedTemplateContext resolvedTemplateContext = new ResolvedTemplateContext(
        resolution.template(), null, resolution.resolvedVersion(),
        resolution.resolvedFrom(), resolution.fallbackApplied(), audienceProfile,
        communicationProfile, resolvedAudience.warnings());
    OutputContract contract = outputContract(resolution.template(),
        audienceProfile, communicationProfile);
    DocumentProvenance provenance = provenance(generationContext,
        resolvedTemplateContext, renderingPlan);
    return new DocumentContext(UUID.randomUUID().toString(),
        DocumentTemplateType.TECHNICAL_DOCUMENTATION,
        generationContext, knowledgeBase,
        resolvedTemplateContext, contract, renderingPlan, provenance,
        mergedWarnings(resolvedAudience.warnings(), renderingPlan.warnings()));
  }

  private TemplateResolutionResult resolveTemplate(String templateId) {
    return templateRepository.resolveActive(templateId)
        .orElseThrow(() -> new IllegalStateException(
            "Template is not registered: " + templateId));
  }

  private OutputContract outputContract(
      DocumentTemplate template, AudienceProfile audienceProfile,
      CommunicationProfile communicationProfile) {
    return new OutputContract("markdown", communicationProfile.defaultLanguage(),
      "Generate documentation from verified knowledge.",
      "No evidenciado en el escaneo.", true,
      template.sections().stream().map(TemplateSection::title)
        .map(title -> "## " + title).toList(),
      List.of("evidence"),
      List.of("Use only provided structured knowledge."),
      List.of("Never invent information."));
  }

  private DocumentProvenance provenance(DocumentationGenerationContext context,
      ResolvedTemplateContext templateContext, RenderingPlan renderingPlan) {
    return new DocumentProvenance(context.scanId(), context.organization(),
        context.projectName(), context.repositoryName(), context.repositoryPath(),
        templateContext.template().id(), templateContext.resolvedVersion().value(),
        templateContext.resolvedFrom(),
        templateContext.audienceProfile().audienceType().name(),
        templateContext.communicationProfile().profileId(),
        renderingPlan.selectedFindingIds().size(),
        renderingPlan.excludedFindingIds().size(),
        renderingPlan.sectionContexts().size(),
        context.codeScanSummary() == null ? 0
            : context.codeScanSummary().evidences().size(),
        Instant.now());
  }

  private List<String> mergedWarnings(List<String> first, List<String> second) {
    List<String> warnings = new ArrayList<>();
    warnings.addAll(first == null ? List.of() : first);
    warnings.addAll(second == null ? List.of() : second);
    return warnings.stream().distinct().toList();
  }
}
