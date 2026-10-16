package com.pichincha.cdemsaspopenaidocumentation.service.mapper;

import com.pichincha.cdemsaspopenaidocumentation.domain.EvidenceDelta;
import com.pichincha.cdemsaspopenaidocumentation.domain.FileChange;
import com.pichincha.cdemsaspopenaidocumentation.domain.RepositoryCoordinates;
import com.pichincha.cdemsaspopenaidocumentation.domain.ScanExecutionSummary;
import com.pichincha.cdemsaspopenaidocumentation.domain.ScanRecord;
import com.pichincha.cdemsaspopenaidocumentation.domain.entity.ScanChangedFileEntity;
import com.pichincha.cdemsaspopenaidocumentation.domain.entity.ScanExecutionEntity;
import com.pichincha.cdemsaspopenaidocumentation.domain.enums.FileChangeTypeEnum;
import com.pichincha.cdemsaspopenaidocumentation.domain.enums.ScanScopeEnum;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class ScanExecutionMapper {

  public ScanExecutionEntity toEntity(ScanRecord scanRecord, EvidenceDelta delta,
      Instant scannedAt) {
    RepositoryCoordinates repository = scanRecord.command().repository();
    return ScanExecutionEntity.builder()
        .id(scanRecord.command().scanId()).repositoryKey(repository.key())
        .organization(repository.organization()).projectName(repository.projectName())
        .repositoryName(repository.repositoryName())
        .pullRequestId(scanRecord.command().pullRequestId())
        .scanScope(scanRecord.plan().scope().name()).scopeReason(scanRecord.plan().reason())
        .checkedOutRef(scanRecord.cloneResult().checkedOutRef())
        .headCommit(scanRecord.cloneResult().headCommit())
        .pullRequestResolved(scanRecord.cloneResult().pullRequestResolved())
        .analyzerVersion(scanRecord.analyzerVersion())
        .filesScanned(scanRecord.plan().filesToScan().size())
        .changedFiles(scanRecord.plan().changes().size())
        .evidenceAdded(delta.added().size()).evidenceRemoved(delta.removed().size())
        .scannedAt(scannedAt)
        .build();
  }

  public ScanExecutionSummary toSummary(ScanExecutionEntity entity) {
    return new ScanExecutionSummary(entity.getId(), ScanScopeEnum.valueOf(entity.getScanScope()),
        entity.getScopeReason(), entity.getCheckedOutRef(), entity.getHeadCommit(),
        entity.isPullRequestResolved(), entity.getAnalyzerVersion(), entity.getFilesScanned(),
        entity.getChangedFiles(), entity.getEvidenceAdded(), entity.getEvidenceRemoved(),
        entity.getScannedAt());
  }

  public ScanChangedFileEntity toEntity(String scanId, FileChange change) {
    return ScanChangedFileEntity.builder().scanId(scanId).filePath(change.path())
        .changeType(change.changeType().name()).build();
  }

  public FileChange toDomain(ScanChangedFileEntity entity) {
    return new FileChange(entity.getFilePath(),
        FileChangeTypeEnum.valueOf(entity.getChangeType()));
  }
}
