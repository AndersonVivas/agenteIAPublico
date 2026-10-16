package com.pichincha.cdemsaspopenaidocumentation.repository;

import com.pichincha.cdemsaspopenaidocumentation.domain.entity.CodeEvidenceEntity;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CodeEvidenceRepository extends JpaRepository<CodeEvidenceEntity, Long> {

  List<CodeEvidenceEntity> findByRepositoryKeyAndSupersededScanIdIsNullOrderByEvidenceId(
      String repositoryKey);

  List<CodeEvidenceEntity> findByIntroducedScanIdOrderByEvidenceId(String scanId);

  List<CodeEvidenceEntity> findBySupersededScanIdOrderByEvidenceId(String scanId);

  @Modifying
  @Query("""
      update CodeEvidenceEntity evidence set evidence.supersededScanId = :scanId
      where evidence.repositoryKey = :repositoryKey and evidence.supersededScanId is null
      """)
  int supersedeAll(@Param("repositoryKey") String repositoryKey,
      @Param("scanId") String scanId);

  @Modifying
  @Query("""
      update CodeEvidenceEntity evidence set evidence.supersededScanId = :scanId
      where evidence.repositoryKey = :repositoryKey and evidence.supersededScanId is null
      and evidence.filePath in :filePaths
      """)
  int supersedeFiles(@Param("repositoryKey") String repositoryKey,
      @Param("filePaths") Collection<String> filePaths, @Param("scanId") String scanId);
}
