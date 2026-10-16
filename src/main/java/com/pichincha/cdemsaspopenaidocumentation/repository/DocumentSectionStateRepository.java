package com.pichincha.cdemsaspopenaidocumentation.repository;

import com.pichincha.cdemsaspopenaidocumentation.domain.entity.DocumentSectionStateEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentSectionStateRepository
    extends JpaRepository<DocumentSectionStateEntity, Long> {

  List<DocumentSectionStateEntity> findByDocumentKey(String documentKey);
}
