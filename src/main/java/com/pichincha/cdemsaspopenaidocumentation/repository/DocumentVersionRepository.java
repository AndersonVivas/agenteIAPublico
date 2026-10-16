package com.pichincha.cdemsaspopenaidocumentation.repository;

import com.pichincha.cdemsaspopenaidocumentation.domain.entity.DocumentVersionEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentVersionRepository extends JpaRepository<DocumentVersionEntity, Long> {

  Optional<DocumentVersionEntity> findFirstByDocumentKeyOrderByVersionNumberDesc(
      String documentKey);

  List<DocumentVersionEntity> findByDocumentKeyOrderByVersionNumberDesc(String documentKey);
}
