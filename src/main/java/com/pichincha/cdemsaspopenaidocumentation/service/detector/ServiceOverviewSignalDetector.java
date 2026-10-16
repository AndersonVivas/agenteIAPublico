package com.pichincha.cdemsaspopenaidocumentation.service.detector;

import com.pichincha.cdemsaspopenaidocumentation.domain.SignalMatch;
import com.pichincha.cdemsaspopenaidocumentation.domain.SignalProfile;
import com.pichincha.cdemsaspopenaidocumentation.domain.SourceFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Captures what the team wrote about the purpose of the service: the prose of the README and
 * the description declared in the build descriptor. It is business context, never a
 * replacement for code evidence.
 */
@Component
@Order(8)
public class ServiceOverviewSignalDetector implements SignalDetector {

  private static final String README_RULE = "SERVICE_README_OVERVIEW";
  private static final String DESCRIPTOR_RULE = "SERVICE_DESCRIPTOR";
  private static final int MAX_README_LINES = 40;
  private static final int MIN_PROSE_LENGTH = 3;
  private static final String CODE_FENCE = "```";
  private static final Set<String> DESCRIPTOR_FILES = Set.of("pom.xml", "build.gradle",
      "build.gradle.kts", "package.json");
  private static final List<String> NON_PROSE_PREFIXES = List.of("![", "<!--", "[!", "|", "---",
      "===", "<");
  private static final Pattern DESCRIPTOR_PATTERN = Pattern.compile(
      "<description>.+</description>|^\\s*description\\s*=|\"description\"\\s*:\\s*\"");

  @Override
  public String signalType() {
    return "BUSINESS_CONTEXT";
  }

  @Override
  public int maxSignals() {
    return 80;
  }

  @Override
  public boolean supports(SourceFile file) {
    return isReadme(file) || DESCRIPTOR_FILES.contains(file.fileName());
  }

  @Override
  public List<SignalMatch> detect(SourceFile file) {
    return isReadme(file) ? readmeProse(file) : IntStream.range(0, file.lines().size())
        .filter(index -> DESCRIPTOR_PATTERN.matcher(file.line(index)).find())
        .mapToObj(index -> new SignalMatch(index, DESCRIPTOR_RULE))
        .toList();
  }

  @Override
  public SignalProfile profile(String matchedRule) {
    double confidence = README_RULE.equals(matchedRule) ? 0.85 : 0.9;
    return SignalProfile.of("SERVICE_PURPOSE_STATEMENT", "low", "documentation", confidence)
        .withGuidance("Statement written by the team about the purpose of the service.",
            "Use it as business context; code evidence prevails when they differ.");
  }

  private List<SignalMatch> readmeProse(SourceFile file) {
    List<SignalMatch> matches = new ArrayList<>();
    boolean insideCode = false;
    for (int index = 0; index < file.lines().size() && matches.size() < MAX_README_LINES;
        index++) {
      String line = file.line(index).trim();
      if (line.startsWith(CODE_FENCE)) {
        insideCode = !insideCode;
      } else if (!insideCode && isProse(line)) {
        matches.add(new SignalMatch(index, README_RULE));
      }
    }
    return matches;
  }

  private boolean isProse(String line) {
    return line.length() >= MIN_PROSE_LENGTH
        && NON_PROSE_PREFIXES.stream().noneMatch(line::startsWith);
  }

  private boolean isReadme(SourceFile file) {
    return "md".equals(file.extension())
        && file.fileName().toLowerCase(Locale.ROOT).startsWith("readme");
  }
}
