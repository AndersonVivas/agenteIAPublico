package com.pichincha.cdemsaspopenaidocumentation.prompt.domain;

public record PromptTemplateDefinition(
    String templateResource,
    String rulesResource,
    String outputFormat) {
}