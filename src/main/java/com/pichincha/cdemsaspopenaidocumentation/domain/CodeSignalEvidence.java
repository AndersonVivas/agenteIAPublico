package com.pichincha.cdemsaspopenaidocumentation.domain;

import java.util.List;

public record CodeSignalEvidence(
    String evidenceId,
    String scanId,
    String signalType,
    String sourceKind,
    String language,
    String technology,
    String filePath,
    String symbolName,
    String scopeSymbol,
    String ruleId,
    String matchedRule,
    String matchText,
    String severityHint,
    String runtimeSurface,
    List<String> dependencySurface,
    String probableCause,
    String recoveryAction,
    int startLine,
    int endLine,
    int matchLine,
    List<String> snippet,
    double confidence,
    List<String> tags) {
}