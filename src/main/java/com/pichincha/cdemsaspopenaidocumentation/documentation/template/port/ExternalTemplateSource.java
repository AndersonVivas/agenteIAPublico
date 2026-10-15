package com.pichincha.cdemsaspopenaidocumentation.documentation.template.port;

import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.RawTemplateDescriptor;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateSourceType;
import java.util.List;
import java.util.Optional;

public interface ExternalTemplateSource {

  TemplateSourceType sourceType();

  List<RawTemplateDescriptor> listAvailable();

  Optional<RawTemplateDescriptor> findBySourceId(String sourceId);
}
