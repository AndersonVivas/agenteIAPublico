package com.pichincha.cdemsaspopenaidocumentation.copilot.support;

import com.pichincha.cdemsaspopenaidocumentation.documentation.audience.domain.AudienceProfile;
import com.pichincha.cdemsaspopenaidocumentation.documentation.communication.domain.CommunicationProfile;
import com.pichincha.cdemsaspopenaidocumentation.documentation.communication.domain.NarrativeStyle;
import com.pichincha.cdemsaspopenaidocumentation.documentation.communication.domain.ReadingLevel;
import com.pichincha.cdemsaspopenaidocumentation.documentation.communication.domain.TechnicalAbstractionLevel;
import com.pichincha.cdemsaspopenaidocumentation.documentation.communication.domain.TerminologyPolicy;
import com.pichincha.cdemsaspopenaidocumentation.documentation.template.domain.ReaderProfile;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 * Describes for the render agent who reads the document and how to write for that reader,
 * turning the audience and communication profiles of the template into explicit writing
 * guidance.
 */
public final class ReaderProfilePromptFormatter {

  private static final String NOT_DEFINED = "no definido";

  private ReaderProfilePromptFormatter() {
  }

  public static String format(ReaderProfile profile) {
    AudienceProfile audience = profile.audience();
    CommunicationProfile communication = profile.communication();
    return String.join("\n",
        "- Lector: " + valueOrDefault(audience.displayName()) + " (" + audience.audienceType()
            + ")",
        "- Contexto del lector: " + valueOrDefault(audience.businessContext()),
        "- Lo que el lector necesita decidir: " + valueOrDefault(audience.decisionFocus()),
        "- Temas que le importan: " + join(audience.primaryConcerns()),
        "- Temas que debes omitir: " + join(audience.excludedConcerns()),
        "- Nivel de lectura: " + readingLevel(communication.readingLevel()),
        "- Nivel tecnico: " + abstraction(communication.technicalAbstractionLevel()),
        "- Estilo narrativo: " + narrative(communication.narrativeStyle()),
        "- Terminologia: " + terminology(communication.terminologyPolicy()),
        "- Terminos preferidos: " + join(communication.preferredTerms()),
        "- Terminos prohibidos: " + join(communication.forbiddenTerms()),
        "- Visibilidad de la evidencia: "
            + valueOrDefault(audience.preferredEvidenceVisibility()));
  }

  private static String readingLevel(ReadingLevel level) {
    return switch (level) {
      case FUNCTIONAL -> "funcional; lectores de negocio sin formacion tecnica";
      case OPERATIONAL -> "operativo; equipos de soporte que siguen procedimientos";
      case TECHNICAL -> "tecnico; desarrolladores que conocen el codigo";
      case EXPERT -> "experto; especialistas que necesitan todo el detalle";
    };
  }

  private static String abstraction(TechnicalAbstractionLevel level) {
    return switch (level) {
      case HIDDEN -> "oculta los detalles tecnicos; describe solo efectos visibles";
      case TRANSLATED -> "traduce cada concepto tecnico a su efecto en el negocio";
      case BALANCED -> "explica en lenguaje claro y conserva el nombre tecnico entre parentesis";
      case EXPLICIT -> "usa nombres tecnicos exactos con una explicacion breve";
      case DEEP_TECHNICAL -> "detalle tecnico completo";
    };
  }

  private static String narrative(NarrativeStyle style) {
    return switch (style) {
      case OPERATIONAL -> "operativo: sintoma, impacto, accion inmediata y validacion";
      case BUSINESS_IMPACT -> "impacto en el negocio: que proceso se afecta y que hacer";
      case IMPLEMENTATION_GUIDANCE -> "guia de implementacion para corregir la causa";
      case ARCHITECTURAL_ASSESSMENT -> "evaluacion de riesgos y decisiones de arquitectura";
      case DIAGNOSTIC -> "diagnostico paso a paso de la causa raiz";
      case AUDIT -> "auditoria de controles y exposicion";
    };
  }

  private static String terminology(TerminologyPolicy policy) {
    return switch (policy) {
      case BUSINESS_FRIENDLY -> "vocabulario del negocio; sin jerga tecnica";
      case TRANSLATED_TECHNICAL -> "explica cada termino tecnico la primera vez que aparece";
      case MIXED -> "combina vocabulario de negocio y tecnico";
      case ENGINEERING_PRECISE -> "terminos de ingenieria precisos";
    };
  }

  private static String join(List<String> values) {
    return values.isEmpty() ? NOT_DEFINED : String.join("; ", values);
  }

  private static String valueOrDefault(String value) {
    return StringUtils.defaultIfBlank(value, NOT_DEFINED);
  }
}
