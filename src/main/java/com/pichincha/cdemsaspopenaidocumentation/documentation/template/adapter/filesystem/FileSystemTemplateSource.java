package com.pichincha.cdemsaspopenaidocumentation.documentation.template.adapter.filesystem;

import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.RawTemplateDescriptor;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.port.ExternalTemplateSource;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class FileSystemTemplateSource implements ExternalTemplateSource {

  private final TemplateDirectoryScanner scanner;
  private final TemplateFileResolver fileResolver;

  public FileSystemTemplateSource(TemplateDirectoryScanner scanner,
      TemplateFileResolver fileResolver) {
    this.scanner = scanner;
    this.fileResolver = fileResolver;
  }


  @Override
  public List<RawTemplateDescriptor> listAvailable() {
    Path root = scanner.rootPath();
    return scanner.scan().stream().map(path -> fileResolver.resolve(root, path))
        .flatMap(Optional::stream).toList();
  }
}
