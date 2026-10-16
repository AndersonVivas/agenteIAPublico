package com.pichincha.cdemsaspopenaidocumentation.documentation.template.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateGuidance;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.support.TemplateNodeReader;
import com.pichincha.cdemsaspopenaidocumentation.domain.enums.DocumentLifecycleEnum;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class TemplateGuidanceParser {

  private static final String LIFECYCLE_FIELD = "lifecycle";
  private static final String GLOBAL_RULES_FIELD = "globalRules";
  private static final String OUTPUT_CONTRACT_FIELD = "outputContract";
  private static final String MISSING_INFORMATION_FIELD = "missingInformationText";
  private static final String SKILLS_FIELD = "skills";
  private static final String EDITORIAL_REVIEW_FIELD = "editorialReview";

  public TemplateGuidance parse(JsonNode definition, List<String> errors) {
    JsonNode outputContract = definition == null ? null : definition.get(OUTPUT_CONTRACT_FIELD);
    return new TemplateGuidance(lifecycle(definition, errors),
        TemplateNodeReader.stringList(definition, GLOBAL_RULES_FIELD),
        TemplateNodeReader.text(outputContract, MISSING_INFORMATION_FIELD),
        TemplateNodeReader.stringList(definition, SKILLS_FIELD),
        definition != null && definition.path(EDITORIAL_REVIEW_FIELD).asBoolean(false));
  }

  private DocumentLifecycleEnum lifecycle(JsonNode definition, List<String> errors) {
    String value = TemplateNodeReader.text(definition, LIFECYCLE_FIELD);
    try {
      return DocumentLifecycleEnum.fromValue(value);
    } catch (IllegalArgumentException exception) {
      errors.add("Unsupported template lifecycle: " + value);
      return DocumentLifecycleEnum.EVOLUTIONARY;
    }
  }
}
