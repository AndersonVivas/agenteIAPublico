package com.pichincha.cdemsaspopenaidocumentation.copilot.strategy;

import com.pichincha.cdemsaspopenaidocumentation.domain.CodeSignalEvidence;
import com.pichincha.cdemsaspopenaidocumentation.domain.EvidenceDelta;
import com.pichincha.cdemsaspopenaidocumentation.domain.enums.DocumentLifecycleEnum;
import com.pichincha.cdemsaspopenaidocumentation.repository.CodeEvidenceRepository;
import com.pichincha.cdemsaspopenaidocumentation.service.mapper.CodeEvidenceMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeltaEvidenceScopeStrategy implements EvidenceScopeStrategy {

  private final CodeEvidenceRepository codeEvidenceRepository;
  private final CodeEvidenceMapper codeEvidenceMapper;

  @Override
  public DocumentLifecycleEnum lifecycle() {
    return DocumentLifecycleEnum.HISTORICAL;
  }

  @Override
  public List<CodeSignalEvidence> select(String repositoryKey, String scanId) {
    List<CodeSignalEvidence> superseded = codeEvidenceRepository
        .findBySupersededScanIdOrderByEvidenceId(scanId).stream()
        .map(codeEvidenceMapper::toDomain).toList();
    List<CodeSignalEvidence> introduced = codeEvidenceRepository
        .findByIntroducedScanIdOrderByEvidenceId(scanId).stream()
        .map(codeEvidenceMapper::toDomain).toList();
    return EvidenceDelta.between(superseded, introduced).added();
  }
}
