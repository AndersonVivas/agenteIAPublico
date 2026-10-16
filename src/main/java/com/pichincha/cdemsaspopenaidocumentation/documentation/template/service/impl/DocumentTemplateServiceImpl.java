package com.pichincha.cdemsaspopenaidocumentation.documentation.template.service.impl;

import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.DocumentTemplate;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.exception.TemplateNotFoundException;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.port.TemplateRepository;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.service.DocumentTemplateService;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.service.TemplateRegistryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DocumentTemplateServiceImpl implements DocumentTemplateService {

  private final TemplateRegistryService templateRegistryService;
  private final TemplateRepository templateRepository;

  @Override
  public List<String> resolveTemplateIds(List<String> requestedTemplateIds) {
    templateRegistryService.registerAvailableTemplates();
    if (requestedTemplateIds == null || requestedTemplateIds.isEmpty()) {
      return templateRepository.activeTemplateIds();
    }
    requestedTemplateIds.forEach(this::requireRegistered);
    return requestedTemplateIds.stream().distinct().toList();
  }

  @Override
  public DocumentTemplate requireTemplate(String templateId) {
    templateRegistryService.registerAvailableTemplates();
    return requireRegistered(templateId);
  }

  private DocumentTemplate requireRegistered(String templateId) {
    return templateRepository.resolveActive(templateId)
        .orElseThrow(() -> new TemplateNotFoundException(templateId));
  }
}
