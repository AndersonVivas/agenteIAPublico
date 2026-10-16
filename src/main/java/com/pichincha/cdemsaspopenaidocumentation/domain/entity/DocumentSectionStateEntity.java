package com.pichincha.cdemsaspopenaidocumentation.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "document_section_state")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class DocumentSectionStateEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "document_key", nullable = false)
  private String documentKey;

  @Column(name = "section_id", nullable = false)
  private String sectionId;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private String fingerprint;

  @Lob
  @Column(nullable = false)
  private String body;

  @Column(name = "document_version", nullable = false)
  private int documentVersion;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;
}
