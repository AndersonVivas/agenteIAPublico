package com.pichincha.cdemsaspopenaidocumentation.knowledge.service.impl;

import com.pichincha.cdemsaspopenaidocumentation.domain.CodeSignalEvidence;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.ApiKnowledge;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.BusinessKnowledge;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.ConfigurationKnowledge;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.DependencyKnowledge;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.EvidenceRef;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.ExceptionKnowledge;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.KnowledgeConflict;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.KnowledgeFinding;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.KnowledgeGap;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.MonitoringKnowledge;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.domain.ProjectKnowledgeBase;
import com.pichincha.cdemsaspopenaidocumentation.knowledge.service.KnowledgeProjectionService;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class DefaultKnowledgeProjectionService implements KnowledgeProjectionService {

  private static final String API = "api";
  private static final String EXCEPTION = "exception";
  private static final String DEPENDENCY = "dependency";
  private static final String CONFIGURATION = "configuration";
  private static final String MONITORING = "monitoring";
  private static final String BUSINESS = "business";
  private static final String BUSINESS_CONTEXT_SIGNAL = "business_context";

  @Override
  public ProjectKnowledgeBase project(List<CodeSignalEvidence> evidences) {
    List<CodeSignalEvidence> source = sourceEvidences(evidences);
    Map<String, List<CodeSignalEvidence>> byCategory = source.stream()
        .collect(Collectors.groupingBy(this::classify, LinkedHashMap::new,
            Collectors.toList()));
    List<KnowledgeGap> gaps = buildGaps(byCategory);
    List<KnowledgeConflict> conflicts = buildConflicts(source);
    return new ProjectKnowledgeBase(
        new ApiKnowledge(buildFindings(byCategory.get(API), API)),
        new ExceptionKnowledge(buildFindings(byCategory.get(EXCEPTION), EXCEPTION)),
        new DependencyKnowledge(buildFindings(byCategory.get(DEPENDENCY), DEPENDENCY)),
        new ConfigurationKnowledge(buildFindings(byCategory.get(CONFIGURATION), CONFIGURATION)),
        new MonitoringKnowledge(buildFindings(byCategory.get(MONITORING), MONITORING)),
        new BusinessKnowledge(buildFindings(byCategory.get(BUSINESS), BUSINESS)),
        gaps, conflicts);
  }

  private List<CodeSignalEvidence> sourceEvidences(List<CodeSignalEvidence> evidences) {
    Map<String, CodeSignalEvidence> byId = new LinkedHashMap<>();
    mergeByEvidenceId(byId, evidences == null ? List.of() : evidences);
    return byId.values().stream()
        .sorted(Comparator.comparing(CodeSignalEvidence::evidenceId)).toList();
  }

  private void mergeByEvidenceId(Map<String, CodeSignalEvidence> byId,
      List<CodeSignalEvidence> source) {
    for (CodeSignalEvidence evidence : source) {
      if (evidence == null || StringUtils.isBlank(evidence.evidenceId())) {
        continue;
      }
      byId.putIfAbsent(evidence.evidenceId().trim(), evidence);
    }
  }

  private List<KnowledgeFinding> buildFindings(List<CodeSignalEvidence> evidences,
      String category) {
    if (evidences == null || evidences.isEmpty()) {
      return List.of();
    }
    return evidences.stream().collect(Collectors.groupingBy(
        evidence -> findingKey(category, evidence), LinkedHashMap::new,
        Collectors.toList())).entrySet().stream().map(entry -> toFinding(
            category, entry.getKey(), entry.getValue()))
        .sorted(Comparator.comparing(KnowledgeFinding::findingId)).toList();
  }

  private KnowledgeFinding toFinding(String category, String normalizedKey,
      List<CodeSignalEvidence> groupedEvidences) {
    List<EvidenceRef> refs = groupedEvidences.stream().map(this::toRef)
        .distinct().sorted(Comparator.comparing(EvidenceRef::evidenceId)).toList();
    String title = groupedEvidences.stream().map(this::findingTitle)
        .filter(StringUtils::isNotBlank).findFirst().orElse("unclassified-finding");
    String summary = groupedEvidences.stream().map(CodeSignalEvidence::matchText)
        .map(this::compact).filter(StringUtils::isNotBlank).distinct().limit(3)
        .collect(Collectors.joining(" | "));
    return new KnowledgeFinding(findingId(category, normalizedKey), category,
        normalizedKey, title, summary, severity(groupedEvidences),
        averageConfidence(groupedEvidences), refs);
  }

  private String findingTitle(CodeSignalEvidence evidence) {
    String rule = normalize(evidence.ruleId());
    String symbol = normalize(evidence.symbolName());
    return StringUtils.isBlank(symbol) ? rule : rule + "::" + symbol;
  }

  private double averageConfidence(List<CodeSignalEvidence> evidences) {
    return evidences.stream().mapToDouble(CodeSignalEvidence::confidence)
        .average().orElse(0.0);
  }

  private String severity(List<CodeSignalEvidence> evidences) {
    return evidences.stream().map(CodeSignalEvidence::severityHint)
        .map(this::normalize).filter(StringUtils::isNotBlank)
        .max(Comparator.comparingInt(this::severityRank)).orElse("unknown");
  }

  private int severityRank(String severity) {
    return switch (severity) {
      case "critical" -> 4;
      case "high" -> 3;
      case "medium" -> 2;
      case "low" -> 1;
      default -> 0;
    };
  }

  private String findingKey(String category, CodeSignalEvidence evidence) {
    return String.join("|", category, normalize(evidence.ruleId()),
        normalize(evidence.filePath()), normalize(evidence.symbolName()),
        normalize(compact(evidence.matchText())));
  }

  private String findingId(String category, String key) {
    return category + "-" + Integer.toHexString(key.hashCode());
  }

  private List<KnowledgeGap> buildGaps(Map<String, List<CodeSignalEvidence>> byCategory) {
    List<KnowledgeGap> gaps = new ArrayList<>();
    addGapIfMissing(byCategory, gaps, API);
    addGapIfMissing(byCategory, gaps, EXCEPTION);
    addGapIfMissing(byCategory, gaps, DEPENDENCY);
    addGapIfMissing(byCategory, gaps, CONFIGURATION);
    addGapIfMissing(byCategory, gaps, MONITORING);
    addGapIfMissing(byCategory, gaps, BUSINESS);
    return gaps.stream().sorted(Comparator.comparing(KnowledgeGap::gapId)).toList();
  }

  private void addGapIfMissing(Map<String, List<CodeSignalEvidence>> byCategory,
      List<KnowledgeGap> gaps, String category) {
    if (!byCategory.containsKey(category) || byCategory.get(category).isEmpty()) {
      gaps.add(new KnowledgeGap("gap-" + category, category,
          "No evidence was classified for " + category + ".", List.of()));
    }
  }

  private List<KnowledgeConflict> buildConflicts(List<CodeSignalEvidence> evidences) {
    return evidences.stream().collect(Collectors.groupingBy(
        evidence -> findingKey(classify(evidence), evidence), LinkedHashMap::new,
        Collectors.toList())).entrySet().stream().map(this::toConflict)
        .filter(conflict -> StringUtils.isNotBlank(conflict.conflictId()))
        .sorted(Comparator.comparing(KnowledgeConflict::conflictId)).toList();
  }

  private KnowledgeConflict toConflict(Map.Entry<String,
      List<CodeSignalEvidence>> groupedFinding) {
    Set<String> severities = groupedFinding.getValue().stream()
        .map(CodeSignalEvidence::severityHint).map(this::normalize)
        .filter(StringUtils::isNotBlank).collect(Collectors.toCollection(LinkedHashSet::new));
    if (severities.size() < 2) {
      return new KnowledgeConflict("", "", "", List.of());
    }
    String category = classify(groupedFinding.getValue().get(0));
    String id = "conflict-" + Integer.toHexString(groupedFinding.getKey().hashCode());
    String description = "Inconsistent severity: " + String.join(",", severities);
    List<EvidenceRef> refs = groupedFinding.getValue().stream().map(this::toRef).toList();
    return new KnowledgeConflict(id, category, description, refs);
  }

  private EvidenceRef toRef(CodeSignalEvidence evidence) {
    return new EvidenceRef(evidence.evidenceId(), evidence.filePath(),
        evidence.ruleId(), normalize(evidence.signalType()), evidence.confidence());
  }

  private String classify(CodeSignalEvidence evidence) {
    String signalType = normalize(evidence.signalType());
    if (API.equals(signalType)) {
      return API;
    }
    if (EXCEPTION.equals(signalType)) {
      return EXCEPTION;
    }
    if (MONITORING.equals(signalType)) {
      return MONITORING;
    }
    if (BUSINESS_CONTEXT_SIGNAL.equals(signalType)) {
      return BUSINESS;
    }
    if (DEPENDENCY.equals(signalType) || "database".equals(signalType)) {
      return DEPENDENCY;
    }
    return isMonitoringEvidence(evidence) ? MONITORING : CONFIGURATION;
  }

  private boolean isMonitoringEvidence(CodeSignalEvidence evidence) {
    String searchable = String.join(" ", normalize(evidence.matchText()),
        normalize(evidence.ruleId()), normalize(evidence.filePath()),
        normalize(String.join(" ", nullSafeList(evidence.tags()))));
    return searchable.contains("actuator") || searchable.contains("health")
      || searchable.contains("readiness") || searchable.contains("liveness")
      || searchable.contains("observability") || searchable.contains("monitoring")
      || searchable.contains("kafka") || searchable.contains("logging")
      || searchable.contains("log4j") || searchable.contains("metrics")
      || searchable.contains("prometheus") || searchable.contains("tracing")
      || searchable.contains("telemetry") || searchable.contains("probes")
      || searchable.contains("helm");
  }

  private List<String> nullSafeList(List<String> values) {
    return values == null ? List.of() : values;
  }

  private String compact(String value) {
    String normalized = normalize(value).replaceAll("\\s+", " ");
    if (normalized.length() <= 160) {
      return normalized;
    }
    return normalized.substring(0, 157) + "...";
  }

  private String normalize(String value) {
    return StringUtils.defaultString(value).trim().toLowerCase(Locale.ROOT);
  }
}
