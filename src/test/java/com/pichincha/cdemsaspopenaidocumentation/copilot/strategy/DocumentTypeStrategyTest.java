package com.pichincha.cdemsaspopenaidocumentation.copilot.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.pichincha.cdemsaspopenaidocumentation.copilot.domain.DocumentTemplateType;
import org.junit.jupiter.api.Test;

class DocumentTypeStrategyTest {

  @Test
  void changelogStrategyShouldProvideCorrectTypeAndTitle() {
    DocumentTypeStrategy strategy = new ChangelogDocumentTypeStrategy();
    assertEquals(DocumentTemplateType.CHANGELOG, strategy.supportedType());
    assertEquals("Changelog", strategy.title());
  }

  @Test
  void codeDocumentationStrategyShouldProvideCorrectTypeAndTitle() {
    DocumentTypeStrategy strategy = new CodeDocumentationDocumentTypeStrategy();
    assertEquals(DocumentTemplateType.CODE_DOCUMENTATION, strategy.supportedType());
    assertEquals("Code Documentation", strategy.title());
  }

  @Test
  void technicalSpecificationStrategyShouldProvideCorrectTypeAndTitle() {
    DocumentTypeStrategy strategy = new TechnicalSpecificationDocumentTypeStrategy();
    assertEquals(DocumentTemplateType.TECHNICAL_SPECIFICATION, strategy.supportedType());
    assertEquals("Technical Specification", strategy.title());
  }

  @Test
  void deploymentGuideStrategyShouldProvideCorrectTypeAndTitle() {
    DocumentTypeStrategy strategy = new DeploymentGuideDocumentTypeStrategy();
    assertEquals(DocumentTemplateType.DEPLOYMENT_GUIDE, strategy.supportedType());
    assertEquals("Deployment Guide", strategy.title());
  }
}
