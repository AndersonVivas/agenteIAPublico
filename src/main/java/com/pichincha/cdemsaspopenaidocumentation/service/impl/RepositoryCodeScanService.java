package com.pichincha.cdemsaspopenaidocumentation.service.impl;

import com.pichincha.cdemsaspopenaidocumentation.domain.CodeSignalEvidence;
import com.pichincha.cdemsaspopenaidocumentation.domain.SignalMatch;
import com.pichincha.cdemsaspopenaidocumentation.domain.SourceFile;
import com.pichincha.cdemsaspopenaidocumentation.helper.EvidenceFactory;
import com.pichincha.cdemsaspopenaidocumentation.helper.FileTypeResolver;
import com.pichincha.cdemsaspopenaidocumentation.helper.SourceFileReader;
import com.pichincha.cdemsaspopenaidocumentation.service.CodeScanService;
import com.pichincha.cdemsaspopenaidocumentation.service.detector.SignalDetector;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Reads every requested file once and applies the signal detectors that support it. Each
 * detector has a maximum number of signals per scan, consumed in file path order so the
 * selection is deterministic.
 */
@Service
@RequiredArgsConstructor
public class RepositoryCodeScanService implements CodeScanService {

  private static final String ANALYZER_VERSION = "3.0.0";

  private final List<SignalDetector> detectors;
  private final SourceFileReader sourceFileReader;
  private final FileTypeResolver fileTypeResolver;
  private final EvidenceFactory evidenceFactory;

  @Override
  public List<CodeSignalEvidence> scanFiles(Path repositoryRoot, List<String> relativePaths,
      String scanId) {
    Map<SignalDetector, Integer> remainingSignals = new HashMap<>();
    detectors.forEach(detector -> remainingSignals.put(detector, detector.maxSignals()));
    return relativePaths.stream()
        .filter(this::isScannable)
        .sorted()
        .map(path -> sourceFileReader.read(repositoryRoot, path))
        .flatMap(Optional::stream)
        .flatMap(file -> scanFile(file, scanId, remainingSignals).stream())
        .toList();
  }

  @Override
  public boolean isScannable(String relativePath) {
    return fileTypeResolver.isScannable(relativePath);
  }

  @Override
  public String analyzerVersion() {
    return ANALYZER_VERSION;
  }

  private List<CodeSignalEvidence> scanFile(SourceFile file, String scanId,
      Map<SignalDetector, Integer> remainingSignals) {
    return detectors.stream()
        .filter(detector -> detector.supports(file))
        .flatMap(detector -> detect(detector, file, scanId, remainingSignals).stream())
        .toList();
  }

  private List<CodeSignalEvidence> detect(SignalDetector detector, SourceFile file,
      String scanId, Map<SignalDetector, Integer> remainingSignals) {
    List<SignalMatch> matches = detector.detect(file).stream()
        .limit(remainingSignals.get(detector))
        .toList();
    remainingSignals.merge(detector, -matches.size(), Integer::sum);
    return matches.stream()
        .map(match -> evidenceFactory.create(file, detector, match, scanId))
        .toList();
  }
}
