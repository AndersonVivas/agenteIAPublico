package com.pichincha.cdemsaspopenaidocumentation.domain;

import java.util.List;
import java.util.Map;

public record CodeScanSummary(
    long totalFiles,
    long javaFiles,
    long yamlFiles,
    long markdownFiles,
    long directories,
    Map<String, Long> filesByLanguage,
    Map<String, Long> filesByExtension,
    List<String> apiIndicators,
    List<String> databaseIndicators,
    List<String> exceptionIndicators,
    List<CodeSignalEvidence> evidences,
    ScanExecutionMetadata metadata) {
}