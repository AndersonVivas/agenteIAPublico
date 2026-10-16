package com.pichincha.cdemsaspopenaidocumentation.service.impl;

import com.pichincha.cdemsaspopenaidocumentation.domain.CodeSignalEvidence;
import com.pichincha.cdemsaspopenaidocumentation.domain.FileManifest;
import com.pichincha.cdemsaspopenaidocumentation.domain.RepositoryCloneResult;
import com.pichincha.cdemsaspopenaidocumentation.domain.ScanCommand;
import com.pichincha.cdemsaspopenaidocumentation.domain.ScanExecutionSummary;
import com.pichincha.cdemsaspopenaidocumentation.domain.ScanPlan;
import com.pichincha.cdemsaspopenaidocumentation.domain.ScanRecord;
import com.pichincha.cdemsaspopenaidocumentation.helper.GitManifestReader;
import com.pichincha.cdemsaspopenaidocumentation.helper.GitRepositoryCloner;
import com.pichincha.cdemsaspopenaidocumentation.helper.ScanBaselineReader;
import com.pichincha.cdemsaspopenaidocumentation.helper.ScanScopeResolver;
import com.pichincha.cdemsaspopenaidocumentation.repository.ScanExecutionRepository;
import com.pichincha.cdemsaspopenaidocumentation.service.CodeScanService;
import com.pichincha.cdemsaspopenaidocumentation.service.RepositoryScanService;
import com.pichincha.cdemsaspopenaidocumentation.service.ScanRecordingService;
import com.pichincha.cdemsaspopenaidocumentation.service.mapper.ScanExecutionMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RepositoryScanServiceImpl implements RepositoryScanService {

  private final ScanExecutionRepository scanExecutionRepository;
  private final ScanExecutionMapper scanExecutionMapper;
  private final GitRepositoryCloner gitRepositoryCloner;
  private final GitManifestReader gitManifestReader;
  private final ScanBaselineReader scanBaselineReader;
  private final ScanScopeResolver scanScopeResolver;
  private final CodeScanService codeScanService;
  private final ScanRecordingService scanRecordingService;

  @Override
  public ScanExecutionSummary scan(ScanCommand command) {
    return scanExecutionRepository.findById(command.scanId())
        .map(scanExecutionMapper::toSummary)
        .orElseGet(() -> cloneAndScan(command));
  }

  private ScanExecutionSummary cloneAndScan(ScanCommand command) {
    RepositoryCloneResult cloneResult = gitRepositoryCloner.cloneRepository(command);
    try {
      return scanClone(command, cloneResult);
    } finally {
      gitRepositoryCloner.release(cloneResult);
    }
  }

  private ScanExecutionSummary scanClone(ScanCommand command, RepositoryCloneResult cloneResult) {
    String analyzerVersion = codeScanService.analyzerVersion();
    FileManifest manifest = gitManifestReader.read(cloneResult.localPath())
        .filter(codeScanService::isScannable);
    ScanPlan plan = scanScopeResolver.resolve(
        scanBaselineReader.contextFor(command, manifest, analyzerVersion));
    log.info("Scanning {} with scope {} ({}): {} files", command.repository().key(),
        plan.scope(), plan.reason(), plan.filesToScan().size());
    List<CodeSignalEvidence> evidences = codeScanService.scanFiles(cloneResult.localPath(),
        plan.filesToScan(), command.scanId());
    return scanRecordingService.record(new ScanRecord(command, cloneResult, manifest, plan,
        evidences, analyzerVersion));
  }
}
