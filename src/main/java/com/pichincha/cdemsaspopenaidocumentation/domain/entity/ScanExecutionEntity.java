package com.pichincha.cdemsaspopenaidocumentation.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "scan_execution")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class ScanExecutionEntity {

  @Id
  private String id;

  @Column(name = "repository_key", nullable = false)
  private String repositoryKey;

  @Column(nullable = false)
  private String organization;

  @Column(name = "project_name", nullable = false)
  private String projectName;

  @Column(name = "repository_name", nullable = false)
  private String repositoryName;

  @Column(name = "pull_request_id")
  private Long pullRequestId;

  @Column(name = "scan_scope", nullable = false)
  private String scanScope;

  @Column(name = "scope_reason", nullable = false)
  private String scopeReason;

  @Column(name = "checked_out_ref")
  private String checkedOutRef;

  @Column(name = "head_commit")
  private String headCommit;

  @Column(name = "pull_request_resolved", nullable = false)
  private boolean pullRequestResolved;

  @Column(name = "analyzer_version", nullable = false)
  private String analyzerVersion;

  @Column(name = "files_scanned", nullable = false)
  private int filesScanned;

  @Column(name = "changed_files", nullable = false)
  private int changedFiles;

  @Column(name = "evidence_added", nullable = false)
  private int evidenceAdded;

  @Column(name = "evidence_removed", nullable = false)
  private int evidenceRemoved;

  @Column(name = "scanned_at", nullable = false)
  private Instant scannedAt;
}
