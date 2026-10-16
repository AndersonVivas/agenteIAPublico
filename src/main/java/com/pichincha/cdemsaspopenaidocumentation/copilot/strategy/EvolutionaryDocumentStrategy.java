package com.pichincha.cdemsaspopenaidocumentation.copilot.strategy;

import com.pichincha.cdemsaspopenaidocumentation.copilot.ChangelogService;
import com.pichincha.cdemsaspopenaidocumentation.copilot.DocumentStateRepository;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentContext;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentPublication;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.PublishedDocument;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.RenderedSections;
import com.pichincha.cdemsaspopenaidocumentation.copilot.impl.DocumentPublisher;
import com.pichincha.cdemsaspopenaidocumentation.copilot.impl.IncrementalSectionPlanner;
import com.pichincha.cdemsaspopenaidocumentation.copilot.support.DocumentPathResolver;
import com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.domain.SectionContext;
import com.pichincha.cdemsaspopenaidocumentation.domain.RepositoryCoordinates;
import com.pichincha.cdemsaspopenaidocumentation.domain.enums.DocumentGenerationStatusEnum;
import com.pichincha.cdemsaspopenaidocumentation.domain.enums.DocumentLifecycleEnum;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Living document updated in place: only sections whose fingerprint changed are regenerated,
 * untouched sections keep their stored body, and every update adds a version to the changelog
 * file associated with the document.
 */
@Component
@RequiredArgsConstructor
public class EvolutionaryDocumentStrategy implements DocumentLifecycleStrategy {

  private final DocumentStateRepository documentStateRepository;
  private final IncrementalSectionPlanner incrementalSectionPlanner;
  private final DocumentPublisher documentPublisher;
  private final ChangelogService changelogService;

  @Override
  public DocumentLifecycleEnum lifecycle() {
    return DocumentLifecycleEnum.EVOLUTIONARY;
  }

  @Override
  public List<SectionContext> impactedSections(DocumentContext context) {
    return incrementalSectionPlanner.impactedSections(context.sections(),
        documentStateRepository.load(documentKey(context)), context.renderSignature());
  }

  @Override
  public DocumentGenerationStatusEnum unchangedStatus() {
    return DocumentGenerationStatusEnum.UP_TO_DATE;
  }

  @Override
  public PublishedDocument publish(DocumentContext context, RenderedSections rendered) {
    String documentKey = documentKey(context);
    RepositoryCoordinates repository = context.generationContext().repository();
    String templateId = context.template().id();
    PublishedDocument published = documentPublisher.publish(new DocumentPublication(context,
        documentKey, DocumentPathResolver.evolutionaryDocument(repository, templateId),
        context.template().name(), rendered, documentStateRepository.load(documentKey)));
    documentStateRepository.save(documentKey, incrementalSectionPlanner.renderedStates(
        context.sections(), rendered, context.renderSignature(), published.version()));
    changelogService.writeChangelog(documentKey,
        DocumentPathResolver.changelog(repository, templateId), context.template().name());
    return published;
  }

  private String documentKey(DocumentContext context) {
    return DocumentPathResolver.evolutionaryKey(context.generationContext().repository(),
        context.template().id());
  }
}
