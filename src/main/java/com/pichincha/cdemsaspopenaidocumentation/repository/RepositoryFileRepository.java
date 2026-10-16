package com.pichincha.cdemsaspopenaidocumentation.repository;

import com.pichincha.cdemsaspopenaidocumentation.domain.entity.RepositoryFileEntity;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RepositoryFileRepository extends JpaRepository<RepositoryFileEntity, Long> {

  List<RepositoryFileEntity> findByRepositoryKey(String repositoryKey);

  @Modifying
  @Query("""
      delete from RepositoryFileEntity file
      where file.repositoryKey = :repositoryKey and file.filePath in :filePaths
      """)
  int deleteFiles(@Param("repositoryKey") String repositoryKey,
      @Param("filePaths") Collection<String> filePaths);
}
