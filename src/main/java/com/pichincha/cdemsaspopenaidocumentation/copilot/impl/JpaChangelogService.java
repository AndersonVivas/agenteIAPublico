package com.pichincha.cdemsaspopenaidocumentation.copilot.impl;

import com.pichincha.cdemsaspopenaidocumentation.copilot.ChangelogService;
import com.pichincha.cdemsaspopenaidocumentation.copilot.DocumentPersistenceService;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.ChangelogEntry;
import com.pichincha.cdemsaspopenaidocumentation.copilot.support.ChangelogMarkdownRenderer;
import com.pichincha.cdemsaspopenaidocumentation.domain.entity.DocumentVersionEntity;
import com.pichincha.cdemsaspopenaidocumentation.repository.DocumentVersionRepository;
import com.pichincha.cdemsaspopenaidocumentation.service.mapper.DocumentVersionMapper;
import java.nio.file.Path;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class JpaChangelogService implements ChangelogService {

  private final DocumentVersionRepository documentVersionRepository;
  private final DocumentVersionMapper documentVersionMapper;
  private final DocumentPersistenceService documentPersistenceService;

  @Override
  @Transactional(readOnly = true)
  public int nextVersion(String documentKey) {
    return documentVersionRepository.findFirstByDocumentKeyOrderByVersionNumberDesc(documentKey)
        .map(DocumentVersionEntity::getVersionNumber)
        .orElse(0) + 1;
  }

  @Override
  @Transactional
  public void record(ChangelogEntry entry) {
    documentVersionRepository.save(documentVersionMapper.toEntity(entry));
  }

  @Override
  @Transactional(readOnly = true)
  public void writeChangelog(String documentKey, Path changelogPath, String documentTitle) {
    List<ChangelogEntry> entries = documentVersionRepository
        .findByDocumentKeyOrderByVersionNumberDesc(documentKey).stream()
        .map(documentVersionMapper::toDomain).toList();
    documentPersistenceService.save(changelogPath,
        ChangelogMarkdownRenderer.render(documentTitle, entries));
  }
}
