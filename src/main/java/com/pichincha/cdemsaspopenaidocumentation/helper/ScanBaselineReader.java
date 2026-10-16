package com.pichincha.cdemsaspopenaidocumentation.helper;

import com.pichincha.cdemsaspopenaidocumentation.domain.FileManifest;
import com.pichincha.cdemsaspopenaidocumentation.domain.ScanCommand;
import com.pichincha.cdemsaspopenaidocumentation.domain.ScanScopeContext;
import com.pichincha.cdemsaspopenaidocumentation.domain.entity.RepositoryFileEntity;
import com.pichincha.cdemsaspopenaidocumentation.domain.entity.ScanExecutionEntity;
import com.pichincha.cdemsaspopenaidocumentation.repository.RepositoryFileRepository;
import com.pichincha.cdemsaspopenaidocumentation.repository.ScanExecutionRepository;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScanBaselineReader {

  private final RepositoryFileRepository repositoryFileRepository;
  private final ScanExecutionRepository scanExecutionRepository;

  public ScanScopeContext contextFor(ScanCommand command, FileManifest currentManifest,
      String analyzerVersion) {
    String repositoryKey = command.repository().key();
    String baselineAnalyzerVersion = scanExecutionRepository
        .findFirstByRepositoryKeyOrderByScannedAtDesc(repositoryKey)
        .map(ScanExecutionEntity::getAnalyzerVersion)
        .orElse(null);
    return new ScanScopeContext(currentManifest, baselineManifest(repositoryKey),
        baselineAnalyzerVersion, analyzerVersion, command.forceFullScan());
  }

  private FileManifest baselineManifest(String repositoryKey) {
    return new FileManifest(repositoryFileRepository.findByRepositoryKey(repositoryKey)
        .stream()
        .collect(Collectors.toMap(RepositoryFileEntity::getFilePath,
            RepositoryFileEntity::getBlobSha)));
  }
}
