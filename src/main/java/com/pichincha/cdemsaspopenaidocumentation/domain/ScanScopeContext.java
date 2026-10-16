package com.pichincha.cdemsaspopenaidocumentation.domain;

public record ScanScopeContext(
    FileManifest currentManifest,
    FileManifest baselineManifest,
    String baselineAnalyzerVersion,
    String analyzerVersion,
    boolean forceFullScan) {

  public boolean hasBaseline() {
    return baselineAnalyzerVersion != null && !baselineManifest.isEmpty();
  }

  public boolean hasCompatibleBaseline() {
    return hasBaseline() && baselineAnalyzerVersion.equals(analyzerVersion);
  }
}
