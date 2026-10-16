package com.pichincha.cdemsaspopenaidocumentation.service.detector;

import com.pichincha.cdemsaspopenaidocumentation.domain.SignalMatch;
import com.pichincha.cdemsaspopenaidocumentation.domain.SourceFile;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.IntStream;

/**
 * Template for detectors that evaluate every line of a supported file independently: the
 * subclass decides whether a line is a signal and which rule describes it.
 */
public abstract class LinePatternSignalDetector implements SignalDetector {

  private final String signalType;
  private final Set<String> extensions;
  private final int maxSignals;

  protected LinePatternSignalDetector(String signalType, Set<String> extensions,
      int maxSignals) {
    this.signalType = signalType;
    this.extensions = extensions;
    this.maxSignals = maxSignals;
  }

  @Override
  public String signalType() {
    return signalType;
  }

  @Override
  public int maxSignals() {
    return maxSignals;
  }

  @Override
  public boolean supports(SourceFile file) {
    return extensions.contains(file.extension()) && supportsFile(file);
  }

  @Override
  public List<SignalMatch> detect(SourceFile file) {
    return IntStream.range(0, file.lines().size())
        .filter(index -> accepts(file, file.line(index)))
        .mapToObj(index -> new SignalMatch(index, classify(file.line(index))))
        .toList();
  }

  protected boolean supportsFile(SourceFile file) {
    return true;
  }

  protected abstract boolean accepts(SourceFile file, String line);

  protected abstract String classify(String line);

  protected static String normalize(String line) {
    return line.trim().toLowerCase(Locale.ROOT);
  }
}
