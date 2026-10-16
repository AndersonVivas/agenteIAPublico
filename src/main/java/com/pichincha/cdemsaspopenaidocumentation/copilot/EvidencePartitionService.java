package com.pichincha.cdemsaspopenaidocumentation.copilot;

import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.EvidencePartitions;
import com.pichincha.cdemsaspopenaidocumentation.domain.CodeSignalEvidence;
import java.util.List;

public interface EvidencePartitionService {

  EvidencePartitions partition(List<CodeSignalEvidence> evidences);
}
