package com.pichincha.cdemsaspopenaidocumentation.copilot.impl;

import com.pichincha.cdemsaspopenaidocumentation.copilot.DocumentStateRepository;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentStateSnapshot;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.SectionState;
import com.pichincha.cdemsaspopenaidocumentation.domain.entity.DocumentSectionStateEntity;
import com.pichincha.cdemsaspopenaidocumentation.repository.DocumentSectionStateRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class JpaDocumentStateRepository implements DocumentStateRepository {

  private final DocumentSectionStateRepository sectionStateRepository;
  private final Clock clock;

  @Override
  @Transactional(readOnly = true)
  public DocumentStateSnapshot load(String documentKey) {
    return new DocumentStateSnapshot(sectionStateRepository.findByDocumentKey(documentKey)
        .stream()
        .collect(Collectors.toMap(DocumentSectionStateEntity::getSectionId, this::toState)));
  }

  @Override
  @Transactional
  public void save(String documentKey, Map<String, SectionState> sectionsById) {
    Map<String, DocumentSectionStateEntity> existing = sectionStateRepository
        .findByDocumentKey(documentKey).stream()
        .collect(Collectors.toMap(DocumentSectionStateEntity::getSectionId,
            Function.identity()));
    Instant updatedAt = clock.instant();
    sectionStateRepository.saveAll(sectionsById.entrySet().stream()
        .map(entry -> merge(existing.get(entry.getKey()), documentKey, entry.getKey(),
            entry.getValue(), updatedAt))
        .toList());
  }

  private DocumentSectionStateEntity merge(DocumentSectionStateEntity current,
      String documentKey, String sectionId, SectionState state, Instant updatedAt) {
    DocumentSectionStateEntity entity = current != null ? current
        : DocumentSectionStateEntity.builder().documentKey(documentKey).sectionId(sectionId)
            .build();
    entity.setTitle(state.title());
    entity.setFingerprint(state.fingerprint());
    entity.setBody(state.body());
    entity.setDocumentVersion(state.documentVersion());
    entity.setUpdatedAt(updatedAt);
    return entity;
  }

  private SectionState toState(DocumentSectionStateEntity entity) {
    return new SectionState(entity.getTitle(), entity.getFingerprint(), entity.getBody(),
        entity.getDocumentVersion());
  }
}
