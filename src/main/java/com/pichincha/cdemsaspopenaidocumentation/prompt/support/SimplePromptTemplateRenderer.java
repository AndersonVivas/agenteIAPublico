package com.pichincha.cdemsaspopenaidocumentation.prompt.support;

import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class SimplePromptTemplateRenderer implements PromptTemplateRenderer {

  @Override
  public String render(String template, Map<String, String> values) {
    String rendered = StringUtils.defaultString(template);
    for (Map.Entry<String, String> entry : values.entrySet()) {
      rendered = rendered.replace("{{" + entry.getKey() + "}}", StringUtils.defaultString(
          entry.getValue()));
    }
    return rendered;
  }
}