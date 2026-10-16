package com.pichincha.cdemsaspopenaidocumentation.copilot.strategy;

import com.pichincha.cdemsaspopenaidocumentation.domain.CodeSignalEvidence;
import com.pichincha.cdemsaspopenaidocumentation.domain.enums.DocumentLifecycleEnum;
import com.pichincha.cdemsaspopenaidocumentation.repository.CodeEvidenceRepository;
import com.pichincha.cdemsaspopenaidocumentation.service.mapper.CodeEvidenceMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SnapshotEvidenceScopeStrategy implements EvidenceScopeStrategy {

  private final CodeEvidenceRepository codeEvidenceRepository;
  private final CodeEvidenceMapper codeEvidenceMapper;

  @Override
  public DocumentLifecycleEnum lifecycle() {
    return DocumentLifecycleEnum.EVOLUTIONARY;
  }

  @Override
  public List<CodeSignalEvidence> select(String repositoryKey, String scanId) {
    return codeEvidenceRepository
        .findByRepositoryKeyAndSupersededScanIdIsNullOrderByEvidenceId(repositoryKey).stream()
        .map(codeEvidenceMapper::toDomain).toList();
  }
}
