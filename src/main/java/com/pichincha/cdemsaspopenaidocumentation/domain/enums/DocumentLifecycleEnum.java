package com.pichincha.cdemsaspopenaidocumentation.domain.enums;

import java.util.Locale;
import org.apache.commons.lang3.StringUtils;

public enum DocumentLifecycleEnum {
  EVOLUTIONARY,
  HISTORICAL;

  public static DocumentLifecycleEnum fromValue(String value) {
    if (StringUtils.isBlank(value)) {
      return EVOLUTIONARY;
    }
    return valueOf(value.trim().toUpperCase(Locale.ROOT));
  }
}
