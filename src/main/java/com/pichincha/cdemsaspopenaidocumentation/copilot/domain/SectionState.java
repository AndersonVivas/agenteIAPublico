package com.pichincha.cdemsaspopenaidocumentation.copilot.domain;

public record SectionState(
    String title,
    String fingerprint,
    String body,
    int documentVersion) {
}
