package com.pichincha.cdemsaspopenaidocumentation.service.detector;

import com.pichincha.cdemsaspopenaidocumentation.domain.SignalMatch;
import com.pichincha.cdemsaspopenaidocumentation.domain.SignalProfile;
import com.pichincha.cdemsaspopenaidocumentation.domain.SourceFile;
import java.util.List;

public interface SignalDetector {

  String signalType();

  int maxSignals();

  boolean supports(SourceFile file);

  List<SignalMatch> detect(SourceFile file);

  SignalProfile profile(String matchedRule);
}
