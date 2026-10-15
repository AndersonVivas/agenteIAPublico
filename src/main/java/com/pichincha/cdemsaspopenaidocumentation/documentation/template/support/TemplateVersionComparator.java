package com.pichincha.cdemsaspopenaidocumentation.documentation.template.support;

import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.TemplateVersion;
import java.util.Comparator;

public class TemplateVersionComparator implements Comparator<TemplateVersion> {

  @Override
  public int compare(TemplateVersion left, TemplateVersion right) {
    int[] leftParts = parts(left == null ? "" : left.value());
    int[] rightParts = parts(right == null ? "" : right.value());
    for (int i = 0; i < 3; i++) {
      if (leftParts[i] != rightParts[i]) {
        return Integer.compare(leftParts[i], rightParts[i]);
      }
    }
    return 0;
  }

  private int[] parts(String version) {
    String[] tokens = version.split("\\.");
    return new int[] {parse(tokens, 0), parse(tokens, 1), parse(tokens, 2)};
  }

  private int parse(String[] tokens, int index) {
    if (index >= tokens.length) {
      return 0;
    }
    try {
      return Integer.parseInt(tokens[index]);
    } catch (NumberFormatException exception) {
      return 0;
    }
  }
}
