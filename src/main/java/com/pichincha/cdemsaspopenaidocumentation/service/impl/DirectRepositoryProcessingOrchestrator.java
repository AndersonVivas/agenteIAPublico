package com.pichincha.cdemsaspopenaidocumentation.service.impl;

import com.pichincha.cdemsaspopenaidocumentation.copilot.DocumentationContextRegistry;
import com.pichincha.cdemsaspopenaidocumentation.copilot.DocumentationGenerationService;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentationGenerationContext;
import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.PromptExecutionResult;
import com.pichincha.cdemsaspopenaidocumentation.domain.CodeScanSummary;
import com.pichincha.cdemsaspopenaidocumentation.domain.DocumentationGenerationRequest;
import com.pichincha.cdemsaspopenaidocumentation.domain.RepositoryCloneRequest;
import com.pichincha.cdemsaspopenaidocumentation.domain.RepositoryCloneResponse;
import com.pichincha.cdemsaspopenaidocumentation.domain.RepositoryCloneResult;
import com.pichincha.cdemsaspopenaidocumentation.helper.GitRepositoryCloner;
import com.pichincha.cdemsaspopenaidocumentation.service.CodeScanService;
import com.pichincha.cdemsaspopenaidocumentation.service.RepositoryProcessingOrchestrator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.temporal", name = "enabled", havingValue = "false",
    matchIfMissing = true)
public class DirectRepositoryProcessingOrchestrator implements RepositoryProcessingOrchestrator {

  private final GitRepositoryCloner gitRepositoryCloner;
  private final CodeScanService codeScanService;
  private final DocumentationContextRegistry documentationContextRegistry;
  private final DocumentationGenerationService documentationGenerationService;

  public DirectRepositoryProcessingOrchestrator(GitRepositoryCloner gitRepositoryCloner,
      CodeScanService codeScanService, DocumentationContextRegistry documentationContextRegistry,
      DocumentationGenerationService documentationGenerationService) {
    this.gitRepositoryCloner = gitRepositoryCloner;
    this.codeScanService = codeScanService;
    this.documentationContextRegistry = documentationContextRegistry;
    this.documentationGenerationService = documentationGenerationService;
  }

  @Override
  public RepositoryCloneResponse process(RepositoryCloneRequest request,
      boolean generateDocumentation, String templateId) {
    RepositoryCloneResult cloneResult = gitRepositoryCloner.cloneRepository(request);
    CodeScanSummary scanSummary = codeScanService.scanRepository(cloneResult.localPath());
    RepositoryCloneResponse response = RepositoryCloneResponse.direct(cloneResult, scanSummary);
    if (!generateDocumentation) {
      return response;
    }
    PromptExecutionResult executionResult = generateDocumentation(request, cloneResult,
        scanSummary, templateId);
    return response.withPromptExecutionResult(executionResult);
  }

  private PromptExecutionResult generateDocumentation(RepositoryCloneRequest request,
      RepositoryCloneResult cloneResult, CodeScanSummary scanSummary, String templateId) {
    DocumentationGenerationContext context = new DocumentationGenerationContext(
        scanSummary.scanId(), request.repositoryName(), cloneResult.localPath(),
        request.organization(), request.projectName(), request.pullRequestId(), scanSummary);
    documentationContextRegistry.register(context);
    return documentationGenerationService.generate(
        new DocumentationGenerationRequest(scanSummary.scanId(), templateId));
  }
}