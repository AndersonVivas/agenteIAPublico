package com.pichincha.cdemsaspopenaidocumentation.prompt.impl;

import com.pichincha.cdemsaspopenaidocumentation.prompt.PromptBuilderService;
import com.pichincha.cdemsaspopenaidocumentation.prompt.PromptTemplateStrategy;
import com.pichincha.cdemsaspopenaidocumentation.prompt.config.PromptBuilderProperties;
import com.pichincha.cdemsaspopenaidocumentation.prompt.domain.PromptContext;
import com.pichincha.cdemsaspopenaidocumentation.prompt.domain.PromptTemplateDefinition;
import com.pichincha.cdemsaspopenaidocumentation.prompt.domain.PromptType;
import com.pichincha.cdemsaspopenaidocumentation.prompt.support.PromptContextFormatter;
import com.pichincha.cdemsaspopenaidocumentation.prompt.support.PromptTemplateLoader;
import com.pichincha.cdemsaspopenaidocumentation.prompt.support.PromptTemplateRenderer;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class DefaultPromptBuilderService implements PromptBuilderService {

  private final Map<PromptType, PromptTemplateStrategy> strategies;
  private final PromptBuilderProperties properties;
  private final PromptTemplateLoader loader;
  private final PromptTemplateRenderer renderer;
  private final PromptContextFormatter contextFormatter;

  public DefaultPromptBuilderService(List<PromptTemplateStrategy> strategies,
      PromptBuilderProperties properties, PromptTemplateLoader loader,
      PromptTemplateRenderer renderer, PromptContextFormatter contextFormatter) {
    this.strategies = new EnumMap<>(PromptType.class);
    for (PromptTemplateStrategy strategy : strategies) {
      this.strategies.put(strategy.supportedType(), strategy);
    }
    this.properties = properties;
    this.loader = loader;
    this.renderer = renderer;
    this.contextFormatter = contextFormatter;
  }

  @Override
  public String buildPrompt(PromptType promptType, PromptContext context) {
    PromptTemplateDefinition definition = resolveDefinition(promptType);
    String template = loader.load(resolveResource(properties.templatesPath(),
        definition.templateResource()));
    String rules = loader.load(resolveResource(properties.rulesPath(),
        definition.rulesResource()));
    return renderer.render(template, templateValues(context, rules, definition.outputFormat()));
  }

  private PromptTemplateDefinition resolveDefinition(PromptType promptType) {
    if (promptType == null) {
      throw new IllegalArgumentException("Unsupported prompt type: null");
    }
    PromptTemplateStrategy strategy = strategies.get(promptType);
    if (strategy == null) {
      throw new IllegalArgumentException("Unsupported prompt type: " + promptType);
    }
    return strategy.definition();
  }

  private Map<String, String> templateValues(PromptContext context, String rules,
      String outputFormat) {
    Map<String, String> values = new LinkedHashMap<>();
    values.put("evidence", contextFormatter.formatEvidenceSection(context));
    values.put("rag_context", contextFormatter.formatRagContext(context.ragContext()));
    values.put("rules", rules);
    values.put("output_format", outputFormat);
    return values;
  }

  private String resolveResource(String basePath, String resourceName) {
    return basePath + "/" + resourceName;
  }
}