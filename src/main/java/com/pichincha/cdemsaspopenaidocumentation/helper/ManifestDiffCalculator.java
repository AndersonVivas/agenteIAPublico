package com.pichincha.cdemsaspopenaidocumentation.helper;

import com.pichincha.cdemsaspopenaidocumentation.domain.FileChange;
import com.pichincha.cdemsaspopenaidocumentation.domain.FileManifest;
import com.pichincha.cdemsaspopenaidocumentation.domain.enums.FileChangeTypeEnum;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;

@Component
public class ManifestDiffCalculator {

  public List<FileChange> diff(FileManifest baseline, FileManifest current) {
    Stream<FileChange> addedOrModified = current.blobsByPath().entrySet().stream()
        .map(entry -> changeOf(baseline, entry))
        .flatMap(Optional::stream);
    Stream<FileChange> deleted = baseline.blobsByPath().keySet().stream()
        .filter(path -> !current.blobsByPath().containsKey(path))
        .map(path -> new FileChange(path, FileChangeTypeEnum.DELETED));
    return Stream.concat(addedOrModified, deleted)
        .sorted(Comparator.comparing(FileChange::path)).toList();
  }

  private Optional<FileChange> changeOf(FileManifest baseline, Map.Entry<String, String> entry) {
    String previousBlob = baseline.blobsByPath().get(entry.getKey());
    if (previousBlob == null) {
      return Optional.of(new FileChange(entry.getKey(), FileChangeTypeEnum.ADDED));
    }
    if (previousBlob.equals(entry.getValue())) {
      return Optional.empty();
    }
    return Optional.of(new FileChange(entry.getKey(), FileChangeTypeEnum.MODIFIED));
  }
}
