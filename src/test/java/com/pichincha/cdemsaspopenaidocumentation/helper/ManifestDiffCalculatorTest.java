package com.pichincha.cdemsaspopenaidocumentation.helper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.pichincha.cdemsaspopenaidocumentation.domain.FileChange;
import com.pichincha.cdemsaspopenaidocumentation.domain.FileManifest;
import com.pichincha.cdemsaspopenaidocumentation.domain.enums.FileChangeTypeEnum;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ManifestDiffCalculatorTest {

  @Test
  void diffShouldClassifyAddedModifiedAndDeletedFilesSortedByPath() {
    FileManifest baseline = new FileManifest(Map.of("a.java", "1", "b.java", "2",
        "c.java", "3"));
    FileManifest current = new FileManifest(Map.of("a.java", "1", "b.java", "20",
        "d.java", "4"));

    List<FileChange> changes = new ManifestDiffCalculator().diff(baseline, current);

    assertEquals(List.of(
        new FileChange("b.java", FileChangeTypeEnum.MODIFIED),
        new FileChange("c.java", FileChangeTypeEnum.DELETED),
        new FileChange("d.java", FileChangeTypeEnum.ADDED)), changes);
  }

  @Test
  void diffShouldReturnNoChangesForIdenticalManifests() {
    FileManifest manifest = new FileManifest(Map.of("a.java", "1"));

    assertEquals(List.of(), new ManifestDiffCalculator().diff(manifest, manifest));
  }
}
