package com.pichincha.cdemsaspopenaidocumentation.documentation.template.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.pichincha.cdemsaspopenaidocumentation.documentation.communication.domain.CommunicationProfile;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.support.TemplateNodeReader;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Component;

/**
 * Reads how to communicate with the reader. Every field not declared in the template keeps
 * the value of the built-in profile of the audience.
 */
@Component
public class TemplateCommunicationParser {

  public CommunicationProfile parse(JsonNode node, CommunicationProfile base,
      List<String> errors) {
    return new CommunicationProfile(
        TemplateNodeReader.textOrDefault(node, "profileId", base.profileId()),
        enumOrDefault(node, "narrativeStyle", base.narrativeStyle(), errors),
        enumOrDefault(node, "presentationMode", base.presentationMode(), errors),
        enumOrDefault(node, "readingLevel", base.readingLevel(), errors),
        enumOrDefault(node, "technicalAbstractionLevel", base.technicalAbstractionLevel(),
            errors),
        enumOrDefault(node, "terminologyPolicy", base.terminologyPolicy(), errors),
        TemplateNodeReader.textOrDefault(node, "defaultLanguage", base.defaultLanguage()),
        TemplateNodeReader.stringListOrDefault(node, "preferredTerms", base.preferredTerms()),
        TemplateNodeReader.stringListOrDefault(node, "forbiddenTerms", base.forbiddenTerms()));
  }

  private <E extends Enum<E>> E enumOrDefault(JsonNode node, String fieldName, E fallback,
      List<String> errors) {
    return Objects.requireNonNullElse(
        TemplateNodeReader.enumValue(node, fieldName, fallback.getDeclaringClass(), errors),
        fallback);
  }
}
