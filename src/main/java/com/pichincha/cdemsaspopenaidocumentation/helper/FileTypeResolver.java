package com.pichincha.cdemsaspopenaidocumentation.helper;

import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class FileTypeResolver {

  public static final Set<String> CONFIG_EXTENSIONS = Set.of("yaml", "yml", "properties",
      "json", "xml", "toml", "ini", "env", "conf");
  private static final String NO_EXTENSION = "no_extension";
  private static final Set<String> IGNORED_DIRECTORIES = Set.of(".git", "build", "target",
      "node_modules", "dist", "out", ".gradle", ".idea");
  private static final Set<String> TEXT_EXTENSIONS = Set.of("java", "kt", "kts", "groovy",
      "gradle", "scala", "js", "jsx", "ts", "tsx", "py", "go", "cs", "php", "rb", "rs", "sql",
      "xml", "yaml", "yml", "json", "properties", "md", "sh", "bash", "toml", "ini", "env",
      "conf");
  private static final Map<String, String> LANGUAGE_BY_EXTENSION = Map.ofEntries(
      Map.entry("java", "java"), Map.entry("kt", "kotlin"), Map.entry("kts", "kotlin"),
      Map.entry("groovy", "groovy"), Map.entry("gradle", "groovy"),
      Map.entry("scala", "scala"), Map.entry("js", "javascript"),
      Map.entry("jsx", "javascript"), Map.entry("ts", "typescript"),
      Map.entry("tsx", "typescript"), Map.entry("py", "python"), Map.entry("go", "go"),
      Map.entry("cs", "csharp"), Map.entry("rb", "ruby"), Map.entry("php", "php"),
      Map.entry("rs", "rust"), Map.entry("sql", "sql"), Map.entry("xml", "xml"),
      Map.entry("yml", "yaml"), Map.entry("yaml", "yaml"), Map.entry("json", "json"),
      Map.entry("properties", "properties"), Map.entry("md", "markdown"),
      Map.entry("sh", "shell"), Map.entry("bash", "shell"));

  public boolean isScannable(String relativePath) {
    return !isIgnored(Path.of(relativePath)) && TEXT_EXTENSIONS.contains(extension(relativePath));
  }

  public String extension(String path) {
    String name = Path.of(path).getFileName().toString();
    int index = name.lastIndexOf('.');
    if (index < 0 || index == name.length() - 1) {
      return NO_EXTENSION;
    }
    return name.substring(index + 1).toLowerCase(Locale.ROOT);
  }

  public String language(String extension) {
    return LANGUAGE_BY_EXTENSION.getOrDefault(extension, "other");
  }

  public String sourceKind(String extension) {
    if (extension.equals("md") || extension.equals("txt")) {
      return "docs";
    }
    if (CONFIG_EXTENSIONS.contains(extension)) {
      return "config";
    }
    return TEXT_EXTENSIONS.contains(extension) ? "code" : "other";
  }

  private boolean isIgnored(Path relativePath) {
    for (Path segment : relativePath) {
      if (IGNORED_DIRECTORIES.contains(segment.toString())) {
        return true;
      }
    }
    return false;
  }
}
