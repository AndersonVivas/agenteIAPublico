package com.pichincha.cdemsaspopenaidocumentation.repository;

import com.pichincha.cdemsaspopenaidocumentation.domain.entity.ScanChangedFileEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScanChangedFileRepository extends JpaRepository<ScanChangedFileEntity, Long> {

  List<ScanChangedFileEntity> findByScanIdOrderByFilePath(String scanId);
}
