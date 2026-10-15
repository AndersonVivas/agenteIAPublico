package com.pichincha.cdemsaspopenaidocumentation.helper;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pichincha.cdemsaspopenaidocumentation.exception.RepositoryCloneException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.Test;

class DefaultGitCommandExecutorTest {

  @Test
  void executeShouldFailWhenCommandExceedsTimeout() {
    DefaultGitCommandExecutor executor = new DefaultGitCommandExecutor();

    RepositoryCloneException exception = assertThrows(RepositoryCloneException.class,
        () -> executor.execute(List.of("sh", "-c", "sleep 2"), Path.of("."),
            Duration.ofMillis(10)));

    assertTrue(exception.getMessage().contains("timed out"));
  }
}