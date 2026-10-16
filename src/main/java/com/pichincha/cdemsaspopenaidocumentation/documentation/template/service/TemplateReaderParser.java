package com.pichincha.cdemsaspopenaidocumentation.documentation.template.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.pichincha.cdemsaspopenaidocumentation.documentation.audience.domain.AudienceProfile;
import com.pichincha.cdemsaspopenaidocumentation.documentation.audience.domain.AudienceType;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.ReaderProfile;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.support.TemplateNodeReader;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Reads who the document is written for: the {@code audience}, plus optional
 * {@code audienceProfile} and {@code communicationProfile} blocks that refine the built-in
 * profile of that audience field by field.
 */
@Component
@RequiredArgsConstructor
public class TemplateReaderParser {

  private static final String AUDIENCE_FIELD = "audience";
  private static final String AUDIENCE_PROFILE_FIELD = "audienceProfile";
  private static final String COMMUNICATION_PROFILE_FIELD = "communicationProfile";

  private final TemplateCommunicationParser communicationParser;

  public ReaderProfile parse(JsonNode definition, List<String> errors) {
    AudienceType audienceType = Objects.requireNonNullElse(TemplateNodeReader.enumValue(
        definition, AUDIENCE_FIELD, AudienceType.class, errors), AudienceType.OPERATIONS);
    ReaderProfile base = ReaderProfile.defaultFor(audienceType);
    return new ReaderProfile(
        audienceProfile(field(definition, AUDIENCE_PROFILE_FIELD), base.audience()),
        communicationParser.parse(field(definition, COMMUNICATION_PROFILE_FIELD),
            base.communication(), errors));
  }

  private AudienceProfile audienceProfile(JsonNode node, AudienceProfile base) {
    return new AudienceProfile(base.audienceType(),
        TemplateNodeReader.textOrDefault(node, "displayName", base.displayName()),
        TemplateNodeReader.textOrDefault(node, "preferredLanguage", base.preferredLanguage()),
        TemplateNodeReader.textOrDefault(node, "businessContext", base.businessContext()),
        TemplateNodeReader.textOrDefault(node, "decisionFocus", base.decisionFocus()),
        TemplateNodeReader.stringListOrDefault(node, "primaryConcerns",
            base.primaryConcerns()),
        TemplateNodeReader.stringListOrDefault(node, "excludedConcerns",
            base.excludedConcerns()),
        TemplateNodeReader.textOrDefault(node, "preferredEvidenceVisibility",
            base.preferredEvidenceVisibility()));
  }

  private JsonNode field(JsonNode definition, String name) {
    return definition == null ? null : definition.get(name);
  }
}
