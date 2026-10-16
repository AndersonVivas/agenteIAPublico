package com.pichincha.cdemsaspopenaidocumentation.service.impl;

import com.pichincha.cdemsaspopenaidocumentation.domain.CodeSignalEvidence;
import com.pichincha.cdemsaspopenaidocumentation.domain.EvidenceDelta;
import com.pichincha.cdemsaspopenaidocumentation.domain.FileChange;
import com.pichincha.cdemsaspopenaidocumentation.domain.ScanExecutionSummary;
import com.pichincha.cdemsaspopenaidocumentation.domain.ScanRecord;
import com.pichincha.cdemsaspopenaidocumentation.domain.entity.RepositoryFileEntity;
import com.pichincha.cdemsaspopenaidocumentation.domain.entity.ScanExecutionEntity;
import com.pichincha.cdemsaspopenaidocumentation.domain.enums.ScanScopeEnum;
import com.pichincha.cdemsaspopenaidocumentation.repository.CodeEvidenceRepository;
import com.pichincha.cdemsaspopenaidocumentation.repository.RepositoryFileRepository;
import com.pichincha.cdemsaspopenaidocumentation.repository.ScanChangedFileRepository;
import com.pichincha.cdemsaspopenaidocumentation.repository.ScanExecutionRepository;
import com.pichincha.cdemsaspopenaidocumentation.service.ScanRecordingService;
import com.pichincha.cdemsaspopenaidocumentation.service.mapper.CodeEvidenceMapper;
import com.pichincha.cdemsaspopenaidocumentation.service.mapper.ScanExecutionMapper;
import com.pichincha.cdemsaspopenaidocumentation.util.CollectionUtil;
import java.time.Clock;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ScanRecordingServiceImpl implements ScanRecordingService {

  private static final int IN_CLAUSE_CHUNK_SIZE = 500;

  private final CodeEvidenceRepository codeEvidenceRepository;
  private final RepositoryFileRepository repositoryFileRepository;
  private final ScanExecutionRepository scanExecutionRepository;
  private final ScanChangedFileRepository scanChangedFileRepository;
  private final CodeEvidenceMapper codeEvidenceMapper;
  private final ScanExecutionMapper scanExecutionMapper;
  private final Clock clock;

  @Override
  @Transactional
  public ScanExecutionSummary record(ScanRecord scanRecord) {
    String repositoryKey = scanRecord.command().repository().key();
    supersedePreviousEvidence(scanRecord, repositoryKey);
    codeEvidenceRepository.saveAll(scanRecord.evidences().stream()
        .map(evidence -> codeEvidenceMapper.toEntity(evidence, repositoryKey)).toList());
    updateBaseline(scanRecord, repositoryKey);
    ScanExecutionEntity execution = scanExecutionRepository.saveAndFlush(
        scanExecutionMapper.toEntity(scanRecord, evidenceDelta(scanRecord), clock.instant()));
    scanChangedFileRepository.saveAll(scanRecord.plan().changes().stream()
        .map(change -> scanExecutionMapper.toEntity(execution.getId(), change)).toList());
    return scanExecutionMapper.toSummary(execution);
  }

  private void supersedePreviousEvidence(ScanRecord scanRecord, String repositoryKey) {
    String scanId = scanRecord.command().scanId();
    if (scanRecord.plan().scope() == ScanScopeEnum.FULL) {
      codeEvidenceRepository.supersedeAll(repositoryKey, scanId);
      return;
    }
    CollectionUtil.partition(scanRecord.plan().invalidatedFiles(), IN_CLAUSE_CHUNK_SIZE)
        .forEach(paths -> codeEvidenceRepository.supersedeFiles(repositoryKey, paths, scanId));
  }

  private void updateBaseline(ScanRecord scanRecord, String repositoryKey) {
    CollectionUtil.partition(scanRecord.plan().invalidatedFiles(), IN_CLAUSE_CHUNK_SIZE)
        .forEach(paths -> repositoryFileRepository.deleteFiles(repositoryKey, paths));
    repositoryFileRepository.saveAll(scanRecord.plan().changes().stream()
        .filter(FileChange::requiresScan)
        .map(change -> RepositoryFileEntity.builder().repositoryKey(repositoryKey)
            .filePath(change.path())
            .blobSha(scanRecord.manifest().blobsByPath().get(change.path())).build())
        .toList());
  }

  private EvidenceDelta evidenceDelta(ScanRecord scanRecord) {
    List<CodeSignalEvidence> superseded = codeEvidenceRepository
        .findBySupersededScanIdOrderByEvidenceId(scanRecord.command().scanId()).stream()
        .map(codeEvidenceMapper::toDomain).toList();
    return EvidenceDelta.between(superseded, scanRecord.evidences());
  }
}
