package com.pichincha.cdemsaspopenaidocumentation.service.mapper;

import com.pichincha.cdemsaspopenaidocumentation.domain.CodeSignalEvidence;
import com.pichincha.cdemsaspopenaidocumentation.domain.entity.CodeEvidenceEntity;
import com.pichincha.cdemsaspopenaidocumentation.helper.JsonListHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CodeEvidenceMapper {

  private final JsonListHelper jsonListHelper;

  public CodeEvidenceEntity toEntity(CodeSignalEvidence evidence, String repositoryKey) {
    return CodeEvidenceEntity.builder()
        .evidenceId(evidence.evidenceId()).repositoryKey(repositoryKey)
        .introducedScanId(evidence.scanId()).signalType(evidence.signalType())
        .sourceKind(evidence.sourceKind()).language(evidence.language())
        .technology(evidence.technology()).filePath(evidence.filePath())
        .symbolName(evidence.symbolName()).scopeSymbol(evidence.scopeSymbol())
        .ruleId(evidence.ruleId()).matchedRule(evidence.matchedRule())
        .matchText(evidence.matchText()).severityHint(evidence.severityHint())
        .runtimeSurface(evidence.runtimeSurface())
        .dependencySurface(jsonListHelper.write(evidence.dependencySurface()))
        .probableCause(evidence.probableCause()).recoveryAction(evidence.recoveryAction())
        .startLine(evidence.startLine()).endLine(evidence.endLine())
        .matchLine(evidence.matchLine()).snippet(jsonListHelper.write(evidence.snippet()))
        .confidence(evidence.confidence()).tags(jsonListHelper.write(evidence.tags()))
        .build();
  }

  public CodeSignalEvidence toDomain(CodeEvidenceEntity entity) {
    return new CodeSignalEvidence(entity.getEvidenceId(), entity.getIntroducedScanId(),
        entity.getSignalType(), entity.getSourceKind(), entity.getLanguage(),
        entity.getTechnology(), entity.getFilePath(), entity.getSymbolName(),
        entity.getScopeSymbol(), entity.getRuleId(), entity.getMatchedRule(),
        entity.getMatchText(), entity.getSeverityHint(), entity.getRuntimeSurface(),
        jsonListHelper.read(entity.getDependencySurface()), entity.getProbableCause(),
        entity.getRecoveryAction(), entity.getStartLine(), entity.getEndLine(),
        entity.getMatchLine(), jsonListHelper.read(entity.getSnippet()),
        entity.getConfidence(), jsonListHelper.read(entity.getTags()));
  }
}
