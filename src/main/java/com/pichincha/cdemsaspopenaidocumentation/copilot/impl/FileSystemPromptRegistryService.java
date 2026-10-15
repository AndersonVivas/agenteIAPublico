package com.pichincha.cdemsaspopenaidocumentation.copilot.impl;

import com.pichincha.cdemsaspopenaidocumentation.copilot.PromptRegistryService;
import com.pichincha.cdemsaspopenaidocumentation.copilot.config.PromptRegistryProperties;
import com.pichincha.cdemsaspopenaidocumentation.copilot.exception.PromptNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class FileSystemPromptRegistryService implements PromptRegistryService {

  private final PromptRegistryProperties properties;

  public FileSystemPromptRegistryService(PromptRegistryProperties properties) {
    this.properties = properties;
  }

  @Override
  public String loadPrompt(String promptId) {
    Path promptPath = resolvePath(promptId);
    if (!Files.isRegularFile(promptPath)) {
      throw new PromptNotFoundException(promptId, promptPath.toString());
    }
    return readContent(promptId, promptPath);
  }

  private Path resolvePath(String promptId) {
    String fileName = StringUtils.defaultString(promptId).trim() + properties.extension();
    return Path.of(properties.path()).resolve(fileName);
  }

  private String readContent(String promptId, Path promptPath) {
    try {
      String content = Files.readString(promptPath);
      if (StringUtils.isBlank(content)) {
        throw new PromptNotFoundException(promptId, promptPath.toString());
      }
      return content;
    } catch (IOException exception) {
      throw new PromptNotFoundException(promptId, promptPath.toString());
    }
  }
}
