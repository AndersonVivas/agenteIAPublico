package com.pichincha.cdemsaspopenaidocumentation.copilot.domain;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;

public record RenderedSections(Map<String, String> bodiesBySectionId, List<String> warnings) {

  public RenderedSections {
    bodiesBySectionId = bodiesBySectionId == null ? Map.of() : Map.copyOf(bodiesBySectionId);
    warnings = warnings == null ? List.of() : List.copyOf(warnings);
  }

  public RenderedSections withWarning(String warning) {
    return new RenderedSections(bodiesBySectionId,
        Stream.concat(warnings.stream(), Stream.of(warning)).toList());
  }

  public boolean contains(String sectionId) {
    return StringUtils.isNotBlank(bodiesBySectionId.get(sectionId));
  }

  public String bodyOf(String sectionId) {
    return bodiesBySectionId.get(sectionId);
  }
}
