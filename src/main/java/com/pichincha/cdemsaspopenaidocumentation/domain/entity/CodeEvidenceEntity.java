package com.pichincha.cdemsaspopenaidocumentation.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "code_evidence")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class CodeEvidenceEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "evidence_id", nullable = false)
  private String evidenceId;

  @Column(name = "repository_key", nullable = false)
  private String repositoryKey;

  @Column(name = "introduced_scan_id", nullable = false)
  private String introducedScanId;

  @Column(name = "superseded_scan_id")
  private String supersededScanId;

  @Column(name = "signal_type", nullable = false)
  private String signalType;

  @Column(name = "source_kind")
  private String sourceKind;

  private String language;

  private String technology;

  @Column(name = "file_path", nullable = false)
  private String filePath;

  @Column(name = "symbol_name")
  private String symbolName;

  @Column(name = "scope_symbol")
  private String scopeSymbol;

  @Column(name = "rule_id")
  private String ruleId;

  @Column(name = "matched_rule")
  private String matchedRule;

  @Lob
  @Column(name = "match_text")
  private String matchText;

  @Column(name = "severity_hint")
  private String severityHint;

  @Column(name = "runtime_surface")
  private String runtimeSurface;

  @Column(name = "dependency_surface")
  private String dependencySurface;

  @Column(name = "probable_cause")
  private String probableCause;

  @Column(name = "recovery_action")
  private String recoveryAction;

  @Column(name = "start_line", nullable = false)
  private int startLine;

  @Column(name = "end_line", nullable = false)
  private int endLine;

  @Column(name = "match_line", nullable = false)
  private int matchLine;

  @Lob
  private String snippet;

  @Column(nullable = false)
  private double confidence;

  private String tags;
}
