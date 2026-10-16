package com.pichincha.cdemsaspopenaidocumentation.copilot.strategy;

import com.pichincha.cdemsaspopenaidocumentation.domain.CodeSignalEvidence;
import com.pichincha.cdemsaspopenaidocumentation.domain.enums.DocumentLifecycleEnum;
import java.util.List;

public interface EvidenceScopeStrategy {

  DocumentLifecycleEnum lifecycle();

  List<CodeSignalEvidence> select(String repositoryKey, String scanId);
}
