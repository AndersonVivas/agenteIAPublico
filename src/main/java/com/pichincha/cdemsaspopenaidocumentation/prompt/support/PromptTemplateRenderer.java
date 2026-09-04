package com.pichincha.cdemsaspopenaidocumentation.prompt.support;

import java.util.Map;

public interface PromptTemplateRenderer {

  String render(String template, Map<String, String> values);
}