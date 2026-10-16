package com.pichincha.cdemsaspopenaidocumentation.copilot.impl;

import com.pichincha.cdemsaspopenaidocumentation.copilot.ChangelogService;
import com.pichincha.cdemsaspopenaidocumentation.copilot.DocumentPersistenceService;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.ChangelogEntry;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentContext;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentHeader;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentPublication;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.PublishedDocument;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.RenderedSections;
import com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.domain.SectionContext;
import com.pichincha.cdemsaspopenaidocumentation.domain.ScanExecutionSummary;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Publication steps shared by every document lifecycle: assign the next version, assemble the
 * Markdown, write the file and register the version in the document history.
 */
@Component
@RequiredArgsConstructor
public class DocumentPublisher {

  private final ChangelogService changelogService;
  private final DocumentPersistenceService documentPersistenceService;
  private final DocumentAssembler documentAssembler;
  private final Clock clock;

  public PublishedDocument publish(DocumentPublication publication) {
    DocumentContext context = publication.context();
    int version = changelogService.nextVersion(publication.documentKey());
    Instant generatedAt = clock.instant();
    String content = documentAssembler.assemble(header(publication, version, generatedAt),
        context.sections(), publication.rendered(), publication.previousState());
    Path written = documentPersistenceService.save(publication.documentPath(), content);
    List<String> updatedSections = updatedSectionTitles(context, publication.rendered());
    changelogService.record(changelogEntry(publication, version, updatedSections,
        generatedAt));
    return new PublishedDocument(written.toString(), version, updatedSections);
  }

  private DocumentHeader header(DocumentPublication publication, int version,
      Instant generatedAt) {
    DocumentContext context = publication.context();
    ScanExecutionSummary scan = context.generationContext().scan();
    return new DocumentHeader(publication.title(), context.template().id(),
        context.template().version().value(), context.lifecycle(),
        context.generationContext().repository().key(), version, scan.scanId(),
        scan.headCommit(), context.generationContext().pullRequestId(), generatedAt);
  }

  private ChangelogEntry changelogEntry(DocumentPublication publication, int version,
      List<String> updatedSections, Instant createdAt) {
    DocumentContext context = publication.context();
    ScanExecutionSummary scan = context.generationContext().scan();
    return new ChangelogEntry(publication.documentKey(),
        context.generationContext().repository().key(), context.template().id(),
        context.lifecycle(), version, publication.documentPath().toString(), scan.scanId(),
        context.generationContext().pullRequestId(), scan.headCommit(), scan.scope(),
        scan.changedFiles(), scan.evidenceAdded(), scan.evidenceRemoved(), updatedSections,
        publication.rendered().warnings(), createdAt);
  }

  private List<String> updatedSectionTitles(DocumentContext context, RenderedSections rendered) {
    return context.sections().stream()
        .filter(section -> rendered.contains(section.sectionId()))
        .map(SectionContext::title).toList();
  }
}
