package com.pichincha.cdemsaspopenaidocumentation.documentation.template.adapter.filesystem;

import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateVersion;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class TemplateVersionResolver {

  private static final Pattern VERSION_PATTERN = Pattern.compile("(?i)v?(\\d+\\.\\d+\\.\\d+)");

  public TemplateVersion resolve(Path path, String declaredVersion) {
    if (StringUtils.isNotBlank(declaredVersion)) {
      return new TemplateVersion(declaredVersion);
    }
    return candidates(path).stream().map(this::extractVersion).flatMap(Optional::stream)
        .findFirst().map(TemplateVersion::new).orElseGet(TemplateVersion::initial);
  }

  private List<String> candidates(Path path) {
    return path == null ? List.of() : List.of(
        fileName(path),
        parentName(path),
        grandParentName(path));
  }

  private Optional<String> extractVersion(String value) {
    Matcher matcher = VERSION_PATTERN.matcher(StringUtils.defaultString(value));
    return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
  }

  private String fileName(Path path) {
    return path.getFileName() == null ? "" : path.getFileName().toString();
  }

  private String parentName(Path path) {
    Path parent = path.getParent();
    return parent == null || parent.getFileName() == null ? "" : parent.getFileName().toString();
  }

  private String grandParentName(Path path) {
    Path parent = path.getParent();
    Path grandParent = parent == null ? null : parent.getParent();
    return grandParent == null || grandParent.getFileName() == null
        ? "" : grandParent.getFileName().toString();
  }
}