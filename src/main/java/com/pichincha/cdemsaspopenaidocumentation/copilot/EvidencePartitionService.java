package com.pichincha.cdemsaspopenaidocumentation.copilot;

import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.EvidencePartitions;
import com.pichincha.cdemsaspopenaidocumentation.domain.CodeScanSummary;

public interface EvidencePartitionService {

  EvidencePartitions partition(CodeScanSummary summary);
}