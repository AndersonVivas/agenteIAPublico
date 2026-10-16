package com.pichincha.cdemsaspopenaidocumentation.copilot.impl;

import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentStateSnapshot;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.RenderedSections;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.SectionState;
import com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.domain.SectionContext;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.EvidenceRef;
import com.pichincha.cdemsaspopenaidocumentation.util.HashUtil;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Computes a per-section fingerprint over everything that shapes its content (render
 * signature, title, objective, instructions, presentation mode, highlights and evidence ids)
 * and compares it with the stored state, so only the sections whose inputs changed are sent
 * to the render agent. Only sections actually rendered get a new stored state, which makes a
 * section the LLM omitted be retried on the next execution instead of being lost.
 */
@Component
public class IncrementalSectionPlanner {

  public String fingerprint(SectionContext section, String renderSignature) {
    return HashUtil.sha256Hex(String.join("|", renderSignature, section.title(),
        section.objective(), String.join(",", section.instructions()),
        section.narrativeIntent(), section.presentationMode().name(),
        String.join(",", section.highlights().stream().sorted().toList()),
        String.join(",", evidenceIds(section))));
  }

  public List<SectionContext> impactedSections(List<SectionContext> sections,
      DocumentStateSnapshot previousState, String renderSignature) {
    return sections.stream()
        .filter(section -> !previousState.fingerprintOf(section.sectionId())
            .map(fingerprint(section, renderSignature)::equals)
            .orElse(false))
        .toList();
  }

  public Map<String, SectionState> renderedStates(List<SectionContext> sections,
      RenderedSections rendered, String renderSignature, int documentVersion) {
    return sections.stream()
        .filter(section -> rendered.contains(section.sectionId()))
        .collect(Collectors.toMap(SectionContext::sectionId,
            section -> new SectionState(section.title(), fingerprint(section, renderSignature),
                rendered.bodyOf(section.sectionId()), documentVersion)));
  }

  private List<String> evidenceIds(SectionContext section) {
    return section.traceabilityRefs().stream().map(EvidenceRef::evidenceId).sorted().toList();
  }
}
