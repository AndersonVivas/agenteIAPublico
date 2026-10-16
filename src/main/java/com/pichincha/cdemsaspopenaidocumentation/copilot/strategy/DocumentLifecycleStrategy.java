package com.pichincha.cdemsaspopenaidocumentation.copilot.strategy;

import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentContext;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.PublishedDocument;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.RenderedSections;
import com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.domain.SectionContext;
import com.pichincha.cdemsaspopenaidocumentation.domain.enums.DocumentGenerationStatusEnum;
import com.pichincha.cdemsaspopenaidocumentation.domain.enums.DocumentLifecycleEnum;
import java.util.List;

public interface DocumentLifecycleStrategy {

  DocumentLifecycleEnum lifecycle();

  List<SectionContext> impactedSections(DocumentContext context);

  DocumentGenerationStatusEnum unchangedStatus();

  PublishedDocument publish(DocumentContext context, RenderedSections rendered);
}
