package com.pichincha.cdemsaspopenaidocumentation.helper;

import com.pichincha.cdemsaspopenaidocumentation.domain.CodeSignalEvidence;
import com.pichincha.cdemsaspopenaidocumentation.domain.SignalMatch;
import com.pichincha.cdemsaspopenaidocumentation.domain.SignalProfile;
import com.pichincha.cdemsaspopenaidocumentation.domain.SourceFile;
import com.pichincha.cdemsaspopenaidocumentation.service.detector.SignalDetector;
import com.pichincha.cdemsaspopenaidocumentation.util.HashUtil;
import java.util.List;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EvidenceFactory {

  private static final int SNIPPET_CONTEXT_LINES = 3;
  private static final int EVIDENCE_HASH_LENGTH = 12;

  private final SymbolLocator symbolLocator;
  private final SignalClassifier signalClassifier;

  public CodeSignalEvidence create(SourceFile file, SignalDetector detector, SignalMatch match,
      String scanId) {
    int lineNumber = match.lineIndex() + 1;
    int start = Math.max(1, lineNumber - SNIPPET_CONTEXT_LINES);
    int end = Math.min(file.lines().size(), lineNumber + SNIPPET_CONTEXT_LINES);
    String matchText = file.line(match.lineIndex()).trim();
    String signalType = detector.signalType();
    String rule = match.matchedRule();
    SignalProfile profile = detector.profile(rule);
    return new CodeSignalEvidence(
        evidenceId(file.relativePath(), signalType, rule, lineNumber, matchText), scanId,
        signalType, file.sourceKind(), file.language(),
        signalClassifier.technology(matchText, file.relativePath()), file.relativePath(),
        symbolLocator.symbolName(file, match.lineIndex(), rule),
        symbolLocator.scopeSymbol(file, match.lineIndex()), profile.ruleId(), rule, matchText,
        profile.severityHint(), profile.runtimeSurface(),
        signalClassifier.dependencySurface(matchText), profile.probableCause(),
        profile.recoveryAction(), start, end, lineNumber, snippet(file, start, end),
        profile.confidence(), signalClassifier.tags(signalType, matchText, rule));
  }

  private String evidenceId(String filePath, String signalType, String rule, int lineNumber,
      String matchText) {
    String seed = filePath + "|" + signalType + "|" + rule + "|" + lineNumber + "|" + matchText;
    return "EV-" + HashUtil.shortSha256Hex(seed, EVIDENCE_HASH_LENGTH);
  }

  private List<String> snippet(SourceFile file, int start, int end) {
    return IntStream.rangeClosed(start, end)
        .mapToObj(line -> line + ": " + file.line(line - 1))
        .toList();
  }
}
