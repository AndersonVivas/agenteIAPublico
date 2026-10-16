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
@Table(name = "document_version")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class DocumentVersionEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "document_key", nullable = false)
  private String documentKey;

  @Column(name = "repository_key", nullable = false)
  private String repositoryKey;

  @Column(name = "template_id", nullable = false)
  private String templateId;

  @Column(nullable = false)
  private String lifecycle;

  @Column(name = "version_number", nullable = false)
  private int versionNumber;

  @Column(name = "document_path", nullable = false)
  private String documentPath;

  @Column(name = "scan_id", nullable = false)
  private String scanId;

  @Column(name = "pull_request_id")
  private Long pullRequestId;

  @Column(name = "head_commit")
  private String headCommit;

  @Column(name = "scan_scope")
  private String scanScope;

  @Column(name = "changed_files", nullable = false)
  private int changedFiles;

  @Column(name = "evidence_added", nullable = false)
  private int evidenceAdded;

  @Column(name = "evidence_removed", nullable = false)
  private int evidenceRemoved;

  @Lob
  @Column(name = "updated_sections")
  private String updatedSections;

  @Lob
  private String warnings;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;
}
