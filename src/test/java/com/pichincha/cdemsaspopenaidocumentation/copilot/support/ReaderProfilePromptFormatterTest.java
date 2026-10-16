package com.pichincha.cdemsaspopenaidocumentation.copilot.support;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pichincha.cdemsaspopenaidocumentation.documentation.audience.domain.AudienceType;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.ReaderProfile;
import org.junit.jupiter.api.Test;

class ReaderProfilePromptFormatterTest {

  @Test
  void formatShouldTranslateFunctionalProfileIntoWritingGuidance() {
    ReaderProfile profile = ReaderProfile.defaultFor(AudienceType.FUNCTIONAL_USERS);

    String prompt = ReaderProfilePromptFormatter.format(profile);

    assertTrue(prompt.contains("(FUNCTIONAL_USERS)"));
    assertTrue(prompt.contains("- Nivel de lectura: funcional"));
    assertTrue(prompt.contains("traduce cada concepto tecnico a su efecto en el negocio"));
    assertTrue(prompt.contains("vocabulario del negocio; sin jerga tecnica"));
    assertTrue(prompt.contains("- Terminos prohibidos: "));
  }
}
