package com.pichincha.cdemsaspopenaidocumentation.domain;

import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public record FileManifest(Map<String, String> blobsByPath) {

  public FileManifest {
    blobsByPath = blobsByPath == null ? Map.of() : Map.copyOf(blobsByPath);
  }

  public static FileManifest empty() {
    return new FileManifest(Map.of());
  }

  public FileManifest filter(Predicate<String> pathFilter) {
    return new FileManifest(blobsByPath.entrySet().stream()
        .filter(entry -> pathFilter.test(entry.getKey()))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
  }

  public boolean isEmpty() {
    return blobsByPath.isEmpty();
  }
}
