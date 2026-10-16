package com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain;

import com.pichincha.cdemsaspopenaidocumentation.documentation.audience.domain.AudienceProfile;
import com.pichincha.cdemsaspopenaidocumentation.documentation.audience.domain.AudienceType;
import com.pichincha.cdemsaspopenaidocumentation.documentation.communication.domain.CommunicationProfile;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

/**
 * Who reads the document generated from a template and how to write for that reader.
 */
public record ReaderProfile(AudienceProfile audience, CommunicationProfile communication) {

  public ReaderProfile {
    Objects.requireNonNull(audience, "Audience profile is required.");
    Objects.requireNonNull(communication, "Communication profile is required.");
  }

  public static ReaderProfile defaultFor(AudienceType audienceType) {
    return new ReaderProfile(AudienceProfile.defaults().get(audienceType),
        CommunicationProfile.defaultsByAudience().get(audienceType));
  }

  public String language() {
    return StringUtils.defaultIfBlank(audience.preferredLanguage(),
        communication.defaultLanguage());
  }
}
