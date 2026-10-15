package com.pichincha.cdemsaspopenaidocumentation.knowledge.service;

import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.ProjectKnowledgeBase;

public interface KnowledgeBaseAggregator {

  ProjectKnowledgeBase aggregate(ProjectKnowledgeBase projectedKnowledge);
}
