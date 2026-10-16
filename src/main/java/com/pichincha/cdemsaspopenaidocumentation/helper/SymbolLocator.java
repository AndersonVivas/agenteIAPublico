package com.pichincha.cdemsaspopenaidocumentation.helper;

import com.pichincha.cdemsaspopenaidocumentation.domain.SourceFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * Finds the code symbol (type, method or annotation) an evidence line belongs to, looking at
 * the same line first and then at a bounded window before and after it.
 */
@Component
public class SymbolLocator {

  private static final String OPENAPI_PATH_RULE = "API_OPENAPI_PATH";
  private static final int SEARCH_WINDOW = 30;
  private static final Pattern TYPE_PATTERN = Pattern.compile(
      "\\b(class|interface|enum|record)\\s+([A-Za-z0-9_]+)");
  private static final Pattern ANNOTATION_PATTERN = Pattern.compile("@([A-Za-z0-9_]+)");
  private static final Pattern METHOD_PATTERN = Pattern.compile(
      "([A-Za-z0-9_]+)\\s*\\([^)]*\\)\\s*\\{?");

  public String symbolName(SourceFile file, int lineIndex, String matchedRule) {
    String sameLine = sameLineSymbol(file.line(lineIndex).trim(), matchedRule);
    return sameLine.isBlank() ? scopeSymbol(file, lineIndex) : sameLine;
  }

  public String scopeSymbol(SourceFile file, int lineIndex) {
    String backward = searchSymbol(file, lineIndex, Math.max(0, lineIndex - SEARCH_WINDOW), -1);
    if (!backward.isBlank()) {
      return backward;
    }
    String forward = searchSymbol(file, lineIndex + 1,
        Math.min(file.lines().size() - 1, lineIndex + SEARCH_WINDOW), 1);
    return forward.isBlank() ? file.fileName() : forward;
  }

  private String sameLineSymbol(String line, String matchedRule) {
    if (OPENAPI_PATH_RULE.equals(matchedRule)) {
      return line.endsWith(":") ? line.substring(0, line.length() - 1) : line;
    }
    String annotation = firstGroup(ANNOTATION_PATTERN, line, 1);
    if (!annotation.isBlank()) {
      return annotation;
    }
    String type = firstGroup(TYPE_PATTERN, line, 2);
    return type.isBlank() ? methodName(line) : type;
  }

  private String searchSymbol(SourceFile file, int from, int to, int step) {
    for (int cursor = from; step > 0 ? cursor <= to : cursor >= to; cursor += step) {
      String line = file.line(cursor).trim();
      String type = firstGroup(TYPE_PATTERN, line, 2);
      String symbol = type.isBlank() ? methodName(line) : type;
      if (!symbol.isBlank()) {
        return symbol;
      }
    }
    return "";
  }

  private String methodName(String line) {
    if (line.startsWith("if") || line.startsWith("for") || line.startsWith("while")) {
      return "";
    }
    return firstGroup(METHOD_PATTERN, line, 1);
  }

  private String firstGroup(Pattern pattern, String line, int group) {
    Matcher matcher = pattern.matcher(line);
    return matcher.find() ? matcher.group(group) : "";
  }
}
