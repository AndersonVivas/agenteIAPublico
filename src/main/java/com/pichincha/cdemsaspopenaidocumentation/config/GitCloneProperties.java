package com.pichincha.cdemsaspopenaidocumentation.config;

import com.pichincha.cdemsaspopenaidocumentation.domain.RepositoryCoordinates;
import java.nio.file.Path;
import java.time.Duration;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.git")
public record GitCloneProperties(
    String baseUrl,
    String username,
    String personalAccessToken,
    Path cloneDirectory,
    Duration cloneTimeout) {

  private static final String FILE_SCHEME = "file:";
  private static final String HTTPS_SCHEME = "https://";
  private static final String HTTP_SCHEME = "http://";

  public GitCloneProperties {
    baseUrl = StringUtils.defaultString(baseUrl).trim();
    cloneDirectory = cloneDirectory == null ? defaultCloneDirectory() : cloneDirectory;
    cloneTimeout = cloneTimeout == null ? Duration.ofMinutes(5) : cloneTimeout;
  }

  public String repositoryUrl(RepositoryCoordinates repository) {
    if (StringUtils.isBlank(baseUrl)) {
      throw new IllegalStateException("app.git.base-url must be configured.");
    }
    if (baseUrl.startsWith(FILE_SCHEME)) {
      return StringUtils.removeEnd(baseUrl, "/") + "/" + repository.key();
    }
    return HTTPS_SCHEME + credentialPrefix() + host() + azureRepositoryPath(repository);
  }

  private String azureRepositoryPath(RepositoryCoordinates repository) {
    return RepositoryCoordinates.safeSegment(repository.organization()) + "/"
        + RepositoryCoordinates.safeSegment(repository.projectName()) + "/_git/"
        + RepositoryCoordinates.safeSegment(repository.repositoryName());
  }

  private String host() {
    String host = StringUtils.removeStart(StringUtils.removeStart(baseUrl, HTTPS_SCHEME),
        HTTP_SCHEME);
    return host.endsWith("/") ? host : host + "/";
  }

  private String credentialPrefix() {
    if (StringUtils.isAnyBlank(username, personalAccessToken)) {
      return "";
    }
    return username.trim() + ":" + personalAccessToken.trim() + "@";
  }

  private static Path defaultCloneDirectory() {
    return Path.of(System.getProperty("user.home"), "Documents",
        "iniciativaIADocumentación", "repositoriosAnalizados");
  }
}
