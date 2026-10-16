package com.pichincha.cdemsaspopenaidocumentation.domain;

import com.pichincha.cdemsaspopenaidocumentation.domain.enums.FileChangeTypeEnum;

public record FileChange(String path, FileChangeTypeEnum changeType) {

  public boolean invalidatesPreviousEvidence() {
    return changeType != FileChangeTypeEnum.ADDED;
  }

  public boolean requiresScan() {
    return changeType != FileChangeTypeEnum.DELETED;
  }
}
