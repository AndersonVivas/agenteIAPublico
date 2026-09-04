package com.pichincha.cdemsaspopenaidocumentation.prompt.impl;

import com.pichincha.cdemsaspopenaidocumentation.domain.CodeScanSummary;
import com.pichincha.cdemsaspopenaidocumentation.domain.RepositoryCloneRequest;
import com.pichincha.cdemsaspopenaidocumentation.domain.RepositoryCloneResponse;
import com.pichincha.cdemsaspopenaidocumentation.prompt.PromptBuilderService;
import com.pichincha.cdemsaspopenaidocumentation.prompt.PromptPreviewService;
import com.pichincha.cdemsaspopenaidocumentation.prompt.domain.PromptContext;
import com.pichincha.cdemsaspopenaidocumentation.prompt.domain.PromptType;
import com.pichincha.cdemsaspopenaidocumentation.prompt.domain.RepositoryMetadata;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class DefaultPromptPreviewService implements PromptPreviewService {

  private final PromptBuilderService promptBuilderService;

  public DefaultPromptPreviewService(PromptBuilderService promptBuilderService) {
    this.promptBuilderService = promptBuilderService;
  }

  @Override
  public String buildPreview(RepositoryCloneRequest request, RepositoryCloneResponse response,
      PromptType promptType) {
    return buildPreview(request, response.codeScanSummary(), response.localPath(), promptType);
  }

  @Override
  public String buildPreview(RepositoryCloneRequest request, CodeScanSummary codeScanSummary,
      String localPath, PromptType promptType) {
    PromptType resolvedType = promptType == null ? PromptType.ERROR_MANUAL : promptType;
    PromptContext context = new PromptContext(repositoryMetadata(request, localPath,
        codeScanSummary), evidences(codeScanSummary), ragContext(codeScanSummary),
        currentDocumentation(codeScanSummary), List.of());
    return promptBuilderService.buildPrompt(resolvedType, context);
  }

  private RepositoryMetadata repositoryMetadata(RepositoryCloneRequest request, String localPath,
      CodeScanSummary codeScanSummary) {
    String technology = codeScanSummary == null || codeScanSummary.evidences().isEmpty()
        ? "" : StringUtils.defaultString(codeScanSummary.evidences().get(0).technology());
    return new RepositoryMetadata(request.repositoryName(), localPath, "", technology,
        Map.of("organization", request.organization(), "projectName", request.projectName()));
  }

  private List<com.pichincha.cdemsaspopenaidocumentation.domain.CodeSignalEvidence> evidences(
      CodeScanSummary codeScanSummary) {
    return codeScanSummary == null ? List.of() : codeScanSummary.evidences();
  }

  private String ragContext(CodeScanSummary codeScanSummary) {
    if (codeScanSummary == null) {
      return "No scan summary available.";
    }
    return "Scan summary: totalFiles=" + codeScanSummary.totalFiles()
        + ", directories=" + codeScanSummary.directories()
        + ", languages=" + summarizeCounters(codeScanSummary.filesByLanguage())
        + ", extensions=" + summarizeCounters(codeScanSummary.filesByExtension())
        + ", signalsExtracted=" + codeScanSummary.metadata().signalsExtracted()
        + ", analyzerVersion=" + codeScanSummary.metadata().analyzerVersion()
        + ", promptVersion=" + codeScanSummary.metadata().promptVersion();
  }

  private String summarizeCounters(Map<String, Long> counters) {
    return counters.entrySet().stream()
        .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder())
            .thenComparing(Map.Entry.comparingByKey()))
        .limit(8)
        .map(entry -> entry.getKey() + ":" + entry.getValue())
        .collect(Collectors.joining(", "));
  }

  private String currentDocumentation(CodeScanSummary codeScanSummary) {
    return codeScanSummary == null ? "" : "";
  }
}