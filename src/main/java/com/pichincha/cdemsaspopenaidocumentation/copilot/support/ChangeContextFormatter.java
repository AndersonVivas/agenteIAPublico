package com.pichincha.cdemsaspopenaidocumentation.copilot.support;

import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentationGenerationContext;
import com.pichincha.cdemsaspopenaidocumentation.domain.ScanExecutionSummary;

/**
 * Renders what changed in the scan (scope, analyzed revision, evidence delta and changed files)
 * so the render agent can describe the evolution of the repository.
 */
public final class ChangeContextFormatter {

  private static final int MAX_LISTED_FILES = 50;

  private ChangeContextFormatter() {
  }

  public static String format(DocumentationGenerationContext context) {
    ScanExecutionSummary scan = context.scan();
    StringBuilder builder = new StringBuilder()
        .append("- Alcance del escaneo: ").append(scan.scope())
        .append(" (").append(scan.scopeReason()).append(")\n")
        .append("- Revision analizada: ").append(scan.checkedOutRef())
        .append(" @ ").append(scan.headCommit()).append('\n')
        .append("- Evidencia nueva: ").append(scan.evidenceAdded())
        .append(" | evidencia retirada: ").append(scan.evidenceRemoved()).append('\n')
        .append("- Archivos cambiados: ").append(context.changedFiles().size());
    context.changedFiles().stream().limit(MAX_LISTED_FILES).forEach(change -> builder
        .append("\n  - ").append(change.changeType()).append(' ').append(change.path()));
    return builder.toString();
  }
}
