package com.pichincha.cdemsaspopenaidocumentation.domain;

public record ScanExecutionMetadata(
    String repositoryFingerprint,
    boolean analyzedBefore,
    String previousCommit,
    String currentCommit,
    String scanMode,
    String promptVersion,
    String analyzerVersion,
    long elapsedMs,
    long filesScanned,
    long filesSkipped,
    long apiSignalsExtracted,
    long databaseSignalsExtracted,
    long exceptionSignalsExtracted,
    long configSignalsExtracted,
    long dependencySignalsExtracted,
    long signalsExtracted,
    long scannedBytes) {
}