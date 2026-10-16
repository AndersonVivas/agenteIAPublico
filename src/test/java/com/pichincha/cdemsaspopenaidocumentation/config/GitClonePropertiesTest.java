package com.pichincha.cdemsaspopenaidocumentation.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.pichincha.cdemsaspopenaidocumentation.domain.RepositoryCoordinates;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class GitClonePropertiesTest {

  private static final RepositoryCoordinates REPOSITORY =
      new RepositoryCoordinates("BancoPichinchaEC", "taa servicios", "sad-repo");

  @Test
  void repositoryUrlShouldBuildAzureDevOpsUrlWithCredentials() {
    GitCloneProperties properties = new GitCloneProperties("https://dev.azure.com/", "user",
        "token", Path.of("/tmp"), null);

    assertEquals("https://user:token@dev.azure.com/BancoPichinchaEC/taa-servicios/_git/sad-repo",
        properties.repositoryUrl(REPOSITORY));
  }

  @Test
  void repositoryUrlShouldOmitCredentialsWhenTheyAreMissing() {
    GitCloneProperties properties = new GitCloneProperties("dev.azure.com", "", "",
        Path.of("/tmp"), null);

    assertEquals("https://dev.azure.com/BancoPichinchaEC/taa-servicios/_git/sad-repo",
        properties.repositoryUrl(REPOSITORY));
  }

  @Test
  void repositoryUrlShouldSupportLocalFileRemotes() {
    GitCloneProperties properties = new GitCloneProperties("file:///tmp/remotes/", null, null,
        Path.of("/tmp"), null);

    assertEquals("file:///tmp/remotes/BancoPichinchaEC/taa-servicios/sad-repo",
        properties.repositoryUrl(REPOSITORY));
  }

  @Test
  void repositoryUrlShouldFailWhenBaseUrlIsMissing() {
    GitCloneProperties properties = new GitCloneProperties(" ", null, null, null, null);

    assertThrows(IllegalStateException.class, () -> properties.repositoryUrl(REPOSITORY));
  }
}
