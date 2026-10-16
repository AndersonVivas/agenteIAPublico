package com.pichincha.cdemsaspopenaidocumentation.copilot.exception;

public class SkillNotFoundException extends RuntimeException {

  public SkillNotFoundException(String skillName, String reason) {
    super("Documentation skill '" + skillName + "' is not available: " + reason);
  }

  public SkillNotFoundException(String skillName, String reason, Throwable cause) {
    super("Documentation skill '" + skillName + "' is not available: " + reason, cause);
  }
}
