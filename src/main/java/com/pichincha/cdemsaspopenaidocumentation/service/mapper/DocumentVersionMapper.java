package com.pichincha.cdemsaspopenaidocumentation.service.mapper;

import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.ChangelogEntry;
import com.pichincha.cdemsaspopenaidocumentation.domain.entity.DocumentVersionEntity;
import com.pichincha.cdemsaspopenaidocumentation.domain.enums.DocumentLifecycleEnum;
import com.pichincha.cdemsaspopenaidocumentation.domain.enums.ScanScopeEnum;
import com.pichincha.cdemsaspopenaidocumentation.helper.JsonListHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DocumentVersionMapper {

  private final JsonListHelper jsonListHelper;

  public DocumentVersionEntity toEntity(ChangelogEntry entry) {
    return DocumentVersionEntity.builder()
        .documentKey(entry.documentKey()).repositoryKey(entry.repositoryKey())
        .templateId(entry.templateId()).lifecycle(entry.lifecycle().name())
        .versionNumber(entry.version()).documentPath(entry.documentPath())
        .scanId(entry.scanId()).pullRequestId(entry.pullRequestId())
        .headCommit(entry.headCommit()).scanScope(entry.scanScope().name())
        .changedFiles(entry.changedFiles()).evidenceAdded(entry.evidenceAdded())
        .evidenceRemoved(entry.evidenceRemoved())
        .updatedSections(jsonListHelper.write(entry.updatedSections()))
        .warnings(jsonListHelper.write(entry.warnings())).createdAt(entry.createdAt())
        .build();
  }

  public ChangelogEntry toDomain(DocumentVersionEntity entity) {
    return new ChangelogEntry(entity.getDocumentKey(), entity.getRepositoryKey(),
        entity.getTemplateId(), DocumentLifecycleEnum.valueOf(entity.getLifecycle()),
        entity.getVersionNumber(), entity.getDocumentPath(), entity.getScanId(),
        entity.getPullRequestId(), entity.getHeadCommit(),
        ScanScopeEnum.valueOf(entity.getScanScope()), entity.getChangedFiles(),
        entity.getEvidenceAdded(), entity.getEvidenceRemoved(),
        jsonListHelper.read(entity.getUpdatedSections()),
        jsonListHelper.read(entity.getWarnings()), entity.getCreatedAt());
  }
}
