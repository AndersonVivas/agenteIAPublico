package com.pichincha.cdemsaspopenaidocumentation.copilot.domain;

public record EvidencePartitions(
    ApiEvidenceSet api,
    ExceptionEvidenceSet exceptions,
    DependencyEvidenceSet dependencies,
    ConfigurationEvidenceSet configuration,
    MonitoringEvidenceSet monitoring,
    BusinessFlowEvidenceSet businessFlow) {
}