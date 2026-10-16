package com.pichincha.cdemsaspopenaidocumentation.exception;

public class ScanNotFoundException extends RuntimeException {

  public ScanNotFoundException(String scanId) {
    super("No stored scan was found for scanId: " + scanId);
  }
}
