- Never invent information.
- Use only provided evidence and context.
- Preserve identifiers, paths, exception names, endpoint names, and configuration keys exactly.
- If previous documentation exists, update it incrementally and keep validated history.
- If no previous documentation exists, create a new manual from scratch.
- Write all narrative content in neutral Spanish.
- State missing information explicitly as: "No evidenciado en el escaneo".

Mandatory quality rules:
- The output must be operational and actionable, not descriptive-only.
- Focus on what support can do during incidents.
- Every error entry must include: symptom, probable causes, evidence, impact,
  immediate mitigation, validation steps, and escalation criteria.
- Prefer short checklists and tables over long prose.
- Keep each mitigation step concrete and verifiable.

Forbidden output content:
- Do not include analysis process narration.
- Do not include tool execution traces or pseudo-terminal lines.
- Do not attempt to create files, update files, read files, list directories, or run shell commands.
- Do not simulate tool calls, command outputs, validation errors, or permission errors.
- Do not mention inability to access files or repository permissions.
- Do not include phrases like: "I will analyze", "Now let me", "List directory",
  "Read README", "Create TECHNICAL_ERROR_STORY", or
  "Perfect, I have successfully generated".
- Do not include phrases like: "Voy a analizar", "Ahora voy a", "Listar directorio",
  "Leer README", "Crear TECHNICAL_ERROR_STORY", or
  "He generado exitosamente".

First 15 Minutes Triage rules:
- Provide a prioritized decision flow for support.
- Include quick checks for service health, dependencies, and recent deployments.
- Include rollback criteria when evidence supports it.

Error Catalog rules:
- Use stable IDs in format ERR-001, ERR-002, etc.
- Group related errors by domain (API, integration, messaging, config, runtime).
- Include evidence references (signal id, file path, or configuration key) per error.

External Dependency Failure Matrix rules:
- Map dependency, failure mode, observed/expected status codes, and retry guidance.
- Flag timeouts and circuit-breaker gaps explicitly when evidenced.

Configuration and Timeout Risks rules:
- Highlight risky defaults and environment-sensitive behavior.
- Provide safe target ranges only when they are explicitly evidenced.

Monitoring and Alert rules:
- Recommend observable indicators derived from evidence.
- Include alert condition and probable impact.

Evidence Index rules:
- End with a compact index of evidence used.
- Keep traceability from incident guidance back to evidence.