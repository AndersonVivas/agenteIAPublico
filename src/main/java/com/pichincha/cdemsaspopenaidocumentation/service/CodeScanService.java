package com.pichincha.cdemsaspopenaidocumentation.service;

import com.pichincha.cdemsaspopenaidocumentation.domain.CodeSignalEvidence;
import java.nio.file.Path;
import java.util.List;

public interface CodeScanService {

  /**
   * Scans the given repository-relative files and returns the code signals found in them,
   * tagging every evidence with the scan identifier.
   */
  List<CodeSignalEvidence> scanFiles(Path repositoryRoot, List<String> relativePaths,
      String scanId);

  /**
   * Tells whether a repository-relative path is a candidate for signal detection.
   */
  boolean isScannable(String relativePath);

  /**
   * Version of the detection rules. Stored evidence produced by another version must be
   * rebuilt with a full scan.
   */
  String analyzerVersion();
}
