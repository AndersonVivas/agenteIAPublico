package com.pichincha.cdemsaspopenaidocumentation.prompt.support;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
public class ClasspathPromptTemplateLoader implements PromptTemplateLoader {

  @Override
  public String load(String resourcePath) {
    ClassPathResource resource = new ClassPathResource(resourcePath);
    try {
      return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException exception) {
      throw new IllegalStateException("Unable to load prompt resource: " + resourcePath,
          exception);
    }
  }
}