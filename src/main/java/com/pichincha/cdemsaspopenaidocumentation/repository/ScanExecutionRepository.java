package com.pichincha.cdemsaspopenaidocumentation.repository;

import com.pichincha.cdemsaspopenaidocumentation.domain.entity.ScanExecutionEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScanExecutionRepository extends JpaRepository<ScanExecutionEntity, String> {

  Optional<ScanExecutionEntity> findFirstByRepositoryKeyOrderByScannedAtDesc(
      String repositoryKey);
}
