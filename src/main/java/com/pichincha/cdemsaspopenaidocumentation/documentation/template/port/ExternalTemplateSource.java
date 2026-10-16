package com.pichincha.cdemsaspopenaidocumentation.documentation.template.port;

import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.RawTemplateDescriptor;
import java.util.List;

public interface ExternalTemplateSource {

  List<RawTemplateDescriptor> listAvailable();
}
