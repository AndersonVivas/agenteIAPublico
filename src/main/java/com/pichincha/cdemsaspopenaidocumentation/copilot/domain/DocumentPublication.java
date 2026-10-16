package com.pichincha.cdemsaspopenaidocumentation.copilot.domain;

import java.nio.file.Path;

public record DocumentPublication(
    DocumentContext context,
    String documentKey,
    Path documentPath,
    String title,
    RenderedSections rendered,
    DocumentStateSnapshot previousState) {
}
