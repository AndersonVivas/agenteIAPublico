package com.pichincha.cdemsaspopenaidocumentation.copilot.impl;

import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentStateSnapshot;
import com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.domain.SectionContext;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.EvidenceRef;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Computes a stable per-section content fingerprint (evidence ids + highlights + narrative
 * intent) and compares it against the previous execution's snapshot to decide which
 * sections actually changed. Unchanged sections are never sent to the Template Render
 * Agent, which is the mechanism that makes document updates evolutionary rather than a
 * full regeneration every time.
 */
public class IncrementalSectionPlanner {

  public String fingerprint(SectionContext section) {
    List<String> evidenceIds = section.traceabilityRefs().stream()
        .map(EvidenceRef::evidenceId).sorted().toList();
    String basis = section.narrativeIntent() + "|"
        + String.join(",", sorted(section.highlights())) + "|"
        + String.join(",", evidenceIds);
    return sha256(basis);
  }

  public List<SectionContext> impactedSections(List<SectionContext> sections,
      DocumentStateSnapshot previousState) {
    List<SectionContext> impacted = new ArrayList<>();
    for (SectionContext section : sections) {
      String currentFingerprint = fingerprint(section);
      String previousFingerprint = previousState.sectionFingerprints().get(section.sectionId());
      if (!currentFingerprint.equals(previousFingerprint)) {
        impacted.add(section);
      }
    }
    return List.copyOf(impacted);
  }

  public DocumentStateSnapshot buildSnapshot(List<SectionContext> sections) {
    return new DocumentStateSnapshot(sections.stream()
        .collect(Collectors.toMap(SectionContext::sectionId, this::fingerprint,
            (first, second) -> first)));
  }

  private List<String> sorted(List<String> values) {
    return values.stream().sorted().toList();
  }

  private String sha256(String value) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
      StringBuilder hex = new StringBuilder();
      for (byte b : hash) {
        hex.append(String.format("%02x", b));
      }
      return hex.toString();
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("SHA-256 is not available in this JVM.", exception);
    }
  }
}
