package com.pichincha.cdemsaspopenaidocumentation.helper;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

public interface GitCommandExecutor {

  String execute(List<String> command, Path workingDirectory,
      Duration timeout);
}