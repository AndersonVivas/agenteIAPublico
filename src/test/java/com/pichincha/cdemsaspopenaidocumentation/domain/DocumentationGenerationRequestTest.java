package com.pichincha.cdemsaspopenaidocumentation.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import org.junit.jupiter.api.Test;

class DocumentationGenerationRequestTest {

  @Test
  void shouldTrimScanId() {
    DocumentationGenerationRequest request = new DocumentationGenerationRequest(
        "  scan-1  ", "  error-manual  ");

    assertEquals("scan-1", request.scanId());
    assertEquals("error-manual", request.templateId());
  }

  @Test
  void shouldFailOnUnknownPromptTypeField() {
    ObjectMapper mapper = new ObjectMapper();
    String payload = """
        {
          \"scanId\": \"scan-1\",
          \"templateId\": \"error-manual\",
          \"promptType\": \"ERROR_MANUAL\",
          \"promptContent\": \"malicious\"
        }
        """;

    assertThrows(UnrecognizedPropertyException.class,
        () -> mapper.readValue(payload, DocumentationGenerationRequest.class));
  }

  @Test
  void shouldFailOnUnknownRepositoryPathField() {
    ObjectMapper mapper = new ObjectMapper();
    String payload = """
        {
          \"scanId\": \"scan-1\",
          \"templateId\": \"error-manual\",
          \"repositoryPath\": \"/tmp/workspace\"
        }
        """;

    assertThrows(UnrecognizedPropertyException.class,
        () -> mapper.readValue(payload, DocumentationGenerationRequest.class));
  }
}
