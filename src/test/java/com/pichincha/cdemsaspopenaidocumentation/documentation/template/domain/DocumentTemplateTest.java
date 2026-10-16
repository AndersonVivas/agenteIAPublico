package com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pichincha.cdemsaspopenaidocumentation.documentation.audience.domain.AudienceType;
import com.pichincha.cdemsaspopenaidocumentation.documentation.communication.domain.NarrativeStyle;
import java.util.List;
import org.junit.jupiter.api.Test;

class DocumentTemplateTest {

  @Test
  void shouldNormalizeAndSortSections() {
    TemplateSection second = new TemplateSection("sec-2", "Second", "", List.of(),
        true, 20);
    TemplateSection first = new TemplateSection("sec-1", "First", "", List.of(),
        true, 10);

    DocumentTemplate template = new DocumentTemplate("  tpl-id  ", "  name  ", null,
        List.of(second, first), null);

    assertEquals("tpl-id", template.id());
    assertEquals("name", template.name());
    assertEquals("0.0.0", template.version().value());
    assertEquals("sec-1", template.sections().get(0).id());
    assertEquals("", template.metadata().checksum());
    assertTrue(template.metadata().active());
    assertEquals(AudienceType.OPERATIONS, template.reader().audience().audienceType());
    assertEquals(NarrativeStyle.OPERATIONAL,
        template.reader().communication().narrativeStyle());
  }

  @Test
  void shouldExposeImmutableSections() {
    DocumentTemplate template = new DocumentTemplate("id", "name", null,
        List.of(new TemplateSection("sec", "S", "", List.of(), true, 1)), null);

    assertTrue(template.sections().contains(template.sections().get(0)));
  }
}
