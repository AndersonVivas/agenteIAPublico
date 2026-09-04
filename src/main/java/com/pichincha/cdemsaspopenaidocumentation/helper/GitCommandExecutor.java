package com.pichincha.cdemsaspopenaidocumentation.helper;

import com.pichincha.cdemsaspopenaidocumentation.domain.CommandExecutionResult;
import java.nio.file.Path;
import java.util.List;

public interface GitCommandExecutor {

  CommandExecutionResult execute(List<String> command, Path workingDirectory);
}