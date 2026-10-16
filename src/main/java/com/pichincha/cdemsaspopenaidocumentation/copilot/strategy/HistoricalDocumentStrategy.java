package com.pichincha.cdemsaspopenaidocumentation.copilot.strategy;

import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentContext;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentPublication;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentStateSnapshot;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentationGenerationContext;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.PublishedDocument;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.RenderedSections;
import com.pichincha.cdemsaspopenaidocumentation.copilot.impl.DocumentPublisher;
import com.pichincha.cdemsaspopenaidocumentation.copilot.support.DocumentPathResolver;
import com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.domain.SectionContext;
import com.pichincha.cdemsaspopenaidocumentation.domain.enums.DocumentGenerationStatusEnum;
import com.pichincha.cdemsaspopenaidocumentation.domain.enums.DocumentLifecycleEnum;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Dated document written once per scan with only the evidence that changed in that scan, so
 * the history of the repository is kept as a sequence of independent documents.
 */
@Component
@RequiredArgsConstructor
public class HistoricalDocumentStrategy implements DocumentLifecycleStrategy {

  private static final DateTimeFormatter TITLE_DATE =
      DateTimeFormatter.ISO_LOCAL_DATE.withZone(ZoneOffset.UTC);

  private final DocumentPublisher documentPublisher;

  @Override
  public DocumentLifecycleEnum lifecycle() {
    return DocumentLifecycleEnum.HISTORICAL;
  }

  @Override
  public List<SectionContext> impactedSections(DocumentContext context) {
    return context.generationContext().evidences().isEmpty() ? List.of() : context.sections();
  }

  @Override
  public DocumentGenerationStatusEnum unchangedStatus() {
    return DocumentGenerationStatusEnum.NO_CHANGES;
  }

  @Override
  public PublishedDocument publish(DocumentContext context, RenderedSections rendered) {
    DocumentationGenerationContext generation = context.generationContext();
    String templateId = context.template().id();
    return documentPublisher.publish(new DocumentPublication(context,
        DocumentPathResolver.historicalKey(generation.repository(), templateId,
            generation.scan().scanId()),
        DocumentPathResolver.historicalDocument(generation.repository(), templateId,
            generation.scan().scannedAt(), generation.pullRequestId()),
        title(context), rendered, DocumentStateSnapshot.empty()));
  }

  private String title(DocumentContext context) {
    DocumentationGenerationContext generation = context.generationContext();
    String pullRequest = generation.pullRequestId() == null ? ""
        : " - PR #" + generation.pullRequestId();
    return context.template().name() + pullRequest + " - "
        + TITLE_DATE.format(generation.scan().scannedAt());
  }
}
