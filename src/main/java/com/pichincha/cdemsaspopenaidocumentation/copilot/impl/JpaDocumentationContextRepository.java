package com.pichincha.cdemsaspopenaidocumentation.copilot.impl;

import com.pichincha.cdemsaspopenaidocumentation.copilot.DocumentationContextRepository;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentationGenerationContext;
import com.pichincha.cdemsaspopenaidocumentation.copilot.strategy.EvidenceScopeStrategy;
import com.pichincha.cdemsaspopenaidocumentation.domain.RepositoryCoordinates;
import com.pichincha.cdemsaspopenaidocumentation.domain.entity.ScanExecutionEntity;
import com.pichincha.cdemsaspopenaidocumentation.domain.enums.DocumentLifecycleEnum;
import com.pichincha.cdemsaspopenaidocumentation.exception.ScanNotFoundException;
import com.pichincha.cdemsaspopenaidocumentation.repository.ScanChangedFileRepository;
import com.pichincha.cdemsaspopenaidocumentation.repository.ScanExecutionRepository;
import com.pichincha.cdemsaspopenaidocumentation.service.mapper.ScanExecutionMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class JpaDocumentationContextRepository implements DocumentationContextRepository {

  private final ScanExecutionRepository scanExecutionRepository;
  private final ScanChangedFileRepository scanChangedFileRepository;
  private final ScanExecutionMapper scanExecutionMapper;
  private final List<EvidenceScopeStrategy> evidenceScopeStrategies;

  @Override
  @Transactional(readOnly = true)
  public DocumentationGenerationContext load(String scanId, DocumentLifecycleEnum lifecycle) {
    ScanExecutionEntity scan = scanExecutionRepository.findById(scanId)
        .orElseThrow(() -> new ScanNotFoundException(scanId));
    return new DocumentationGenerationContext(coordinates(scan), scan.getPullRequestId(),
        scanExecutionMapper.toSummary(scan),
        evidenceScopeFor(lifecycle).select(scan.getRepositoryKey(), scanId),
        scanChangedFileRepository.findByScanIdOrderByFilePath(scanId).stream()
            .map(scanExecutionMapper::toDomain).toList());
  }

  private EvidenceScopeStrategy evidenceScopeFor(DocumentLifecycleEnum lifecycle) {
    return evidenceScopeStrategies.stream()
        .filter(strategy -> strategy.lifecycle() == lifecycle)
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("No evidence scope for " + lifecycle));
  }

  private RepositoryCoordinates coordinates(ScanExecutionEntity scan) {
    return new RepositoryCoordinates(scan.getOrganization(), scan.getProjectName(),
        scan.getRepositoryName());
  }
}
