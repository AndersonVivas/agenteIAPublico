package com.pichincha.cdemsaspopenaidocumentation.documentation.rendering.domain;

import com.pichincha.cdemsaspopenaidocumentation.documentation.audience.domain.AudienceType;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateVersion;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

public record RenderingPlan(
    String templateId,
    String templateName,
    TemplateVersion templateVersion,
    AudienceType audienceType,
    String communicationProfileId,
    String documentObjective,
    List<String> selectedFindingIds,
    List<String> excludedFindingIds,
    List<SectionContext> sectionContexts,
    Map<String, List<String>> traceabilityIndex,
    List<String> warnings) {

  public RenderingPlan {
    templateId = normalize(templateId);
    templateName = normalize(templateName);
    templateVersion = templateVersion == null ? TemplateVersion.initial()
        : templateVersion;
    communicationProfileId = normalize(communicationProfileId);
    documentObjective = normalize(documentObjective);
    selectedFindingIds = selectedFindingIds == null
        ? List.of() : List.copyOf(selectedFindingIds);
    excludedFindingIds = excludedFindingIds == null
        ? List.of() : List.copyOf(excludedFindingIds);
    sectionContexts = sectionContexts == null ? List.of() : List.copyOf(sectionContexts);
    traceabilityIndex = traceabilityIndex == null ? Map.of() : Map.copyOf(traceabilityIndex);
    warnings = warnings == null ? List.of() : List.copyOf(warnings);
  }

  private static String normalize(String value) {
    return StringUtils.defaultString(value).trim();
  }
}
