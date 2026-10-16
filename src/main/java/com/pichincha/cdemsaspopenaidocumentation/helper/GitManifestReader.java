package com.pichincha.cdemsaspopenaidocumentation.helper;

import com.pichincha.cdemsaspopenaidocumentation.config.GitCloneProperties;
import com.pichincha.cdemsaspopenaidocumentation.domain.FileManifest;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GitManifestReader {

  private static final String ENTRY_SEPARATOR = "\0";
  private static final String BLOB_TYPE = "blob";

  private final GitCloneProperties properties;
  private final GitCommandExecutor commandExecutor;

  public FileManifest read(Path repositoryDirectory) {
    String output = commandExecutor.execute(List.of("git", "ls-tree", "-r", "-z", "HEAD"),
        repositoryDirectory, properties.cloneTimeout());
    Map<String, String> blobsByPath = Arrays.stream(output.split(ENTRY_SEPARATOR))
        .map(this::parseEntry)
        .flatMap(Optional::stream)
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
            (first, second) -> first));
    return new FileManifest(blobsByPath);
  }

  private Optional<Map.Entry<String, String>> parseEntry(String entry) {
    int tabIndex = entry.indexOf('\t');
    if (tabIndex < 0) {
      return Optional.empty();
    }
    String[] metadata = entry.substring(0, tabIndex).trim().split(" ");
    if (metadata.length < 3 || !BLOB_TYPE.equals(metadata[1])) {
      return Optional.empty();
    }
    return Optional.of(Map.entry(entry.substring(tabIndex + 1), metadata[2]));
  }
}
