package com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.service;

import com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.domain.SectionContext;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class TraceabilityAssembler {

  public Map<String, List<String>> assemble(List<SectionContext> sections) {
    if (sections == null || sections.isEmpty()) {
      return Map.of();
    }
    return sections.stream().collect(Collectors.toMap(SectionContext::sectionId,
        this::evidenceIds, (left, right) -> left, java.util.LinkedHashMap::new));
  }

  private List<String> evidenceIds(SectionContext section) {
    return section.traceabilityRefs().stream().map(ref -> ref.evidenceId())
        .distinct().sorted().toList();
  }
}
