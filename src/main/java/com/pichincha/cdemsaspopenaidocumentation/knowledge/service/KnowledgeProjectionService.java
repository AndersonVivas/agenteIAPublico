package com.pichincha.cdemsaspopenaidocumentation.knowledge.service;

import com.pichincha.cdemsaspopenaidocumentation.domain.CodeScanSummary;
import com.pichincha.cdemsaspopenaidocumentation.domain.CodeSignalEvidence;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.ProjectKnowledgeBase;
import java.util.List;

public interface KnowledgeProjectionService {

  ProjectKnowledgeBase project(CodeScanSummary summary,
      List<CodeSignalEvidence> evidences);
}
