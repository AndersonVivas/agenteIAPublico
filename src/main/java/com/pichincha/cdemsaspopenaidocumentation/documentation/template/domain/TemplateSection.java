package com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain;

import java.util.List;
import org.apache.commons.lang3.StringUtils;

public record TemplateSection(
    String id,
    String title,
    String objective,
    List<String> instructions,
    boolean required,
    int order) {

  public TemplateSection {
    id = StringUtils.defaultString(id).trim();
    title = StringUtils.defaultString(title).trim();
    objective = StringUtils.defaultString(objective).trim();
    instructions = instructions == null ? List.of()
        : instructions.stream().map(StringUtils::defaultString).map(String::trim)
            .filter(StringUtils::isNotBlank).toList();
  }
}
