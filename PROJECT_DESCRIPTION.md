# Project Description

Spring Boot 4 microservice that keeps repository documentation up to date from pull requests.
It scans the code that changed, extracts structured knowledge with LLM agents executed through
the GitHub Copilot CLI, and renders documents from external templates and prompts. Every
execution runs as a Temporal workflow.

## Endpoint

`POST /api/v1/repository-agent/clone?generateDocumentation=true`

```json
{
  "organization": "BancoPichinchaEC",
  "projectName": "my-project",
  "repositoryName": "my-repository",
  "pullRequestId": 123,
  "templateIds": ["error-manual"],
  "forceFullScan": false
}
```

- `templateIds` is optional; when empty, every active template is generated.
- `forceFullScan` rebuilds the evidence of the repository from scratch.
- The response contains the scan summary and one result per template
  (`GENERATED`, `UP_TO_DATE`, `NO_CHANGES` or `FAILED`).
- A second request for the same repository while one is running returns `409`.

## Flow

1. **Scan activity** (single Temporal activity, local file system only):
   - Shallow-clones the repository and checks out `refs/pull/{id}/merge`; when the ref is not
     available it falls back to the default branch.
   - Reads the git blob hash of every scannable file and compares it with the baseline stored
     in H2. Only added or modified files are scanned (`INCREMENTAL`). A full scan happens when
     there is no baseline, the analyzer version changed, or it is forced.
   - Signal detectors (`service/detector/`) extract evidence with its surrounding code lines:
     endpoints, OpenAPI business descriptions, data access, exceptions, error message
     catalogs, configuration, dependencies and the service purpose stated in the README and
     build descriptor.
   - Supersedes the evidence of modified and deleted files, stores the new evidence and the new
     baseline in one transaction, then deletes the clone.
2. **Documentation activity** (one per template):
   - `EVOLUTIONARY` templates use the current evidence snapshot. Only the sections whose
     fingerprint changed (evidence, template content, render prompt or model) are regenerated;
     the rest keep their stored body. Each update writes a new version and the changelog
     associated with the document.
   - `HISTORICAL` templates use only the evidence that changed in the scan and write a new
     dated document; nothing is written when the scan changed no evidence.
   - Specialist agents, consolidation, rendering and the optional editorial review run through
     the Copilot CLI. Evidence ids cited by the LLM are validated against the scanned evidence.

### Copilot works only with the evidence

The repository clone no longer exists when the documentation activity starts. Every Copilot
CLI call runs in an empty temporary directory with `--deny-tool=shell, write, read, url,
memory`, `--disable-builtin-mcps`, `--no-custom-instructions` and `--no-ask-user`, so the
model cannot read files, run commands or browse; it only sees the evidence in the prompt.

## Templates and prompts

Templates (`documentation.templates.path`) are YAML, JSON or Markdown files. A template is
written for one reader. Besides its sections and instructions, it declares:

```yaml
id: error-manual-funcional
name: Guia de Errores para Negocio
version: 1.0.0
status: active               # inactive templates are not generated
lifecycle: EVOLUTIONARY      # or HISTORICAL
editorialReview: true        # optional second writing pass, see below
skills: [lenguaje-claro, glosario, trazabilidad-discreta]
audience: FUNCTIONAL_USERS   # OPERATIONS, DEVELOPERS, ARCHITECTS, SRE or SECURITY
audienceProfile:             # optional; refines the built-in profile of the audience
  displayName: Usuarios de negocio
  primaryConcerns: [Impacto en el negocio]
communicationProfile:        # optional; refines the built-in profile of the audience
  readingLevel: FUNCTIONAL
  terminologyPolicy: BUSINESS_FRIENDLY
globalRules:
  - Never invent information.
outputContract:
  missingInformationText: No evidenciado en el escaneo.
```

- Fields not declared in `audienceProfile` or `communicationProfile` keep the built-in profile
  of the audience.
- Keys the pipeline does not read are reported as warnings in the log when the templates are
  loaded, so they never become silent configuration.
- The reader profile and the skills are injected into the render prompt. Skills
  (`documentation.skills.path`) follow the Agent Skills format: `<skill-name>/SKILL.md` with
  `name` and `description` front matter followed by the writing instructions. Changing a skill
  regenerates the sections of the templates that use it.
- `editorialReview: true` adds an editorial agent after rendering. It rewrites the regenerated
  sections for the reader applying the skills; a revision is accepted only when it cites
  exactly the same evidence ids as its draft. If the editorial call fails, the drafts are
  published and the failure is reported as a warning of the document. It costs one extra
  Copilot call per regenerated document.

Prompts (`documentation.prompts.path`) are external Markdown files with `{{placeholders}}`.

## Output layout

```
<documentation.output-root>/<organization>/<project>/<repository>/
  <template>.md                      evolutionary document
  <template>.changelog.md            changelog of the evolutionary document
  historial/<template>/<yyyyMMdd-HHmmss>-pr-<id>.md   historical documents
```

## Persistence (H2 + Flyway)

| Table | Purpose |
|---|---|
| `scan_execution` | One row per scan: scope, analyzed revision, evidence delta |
| `scan_changed_file` | Files added, modified or deleted in each scan |
| `repository_file` | Baseline: git blob hash per file of the last scan |
| `code_evidence` | Evidence with the scan that introduced and superseded it |
| `document_section_state` | Fingerprint and body of every section of evolutionary documents |
| `document_version` | Version history used to render changelogs |

The H2 console is available at `/h2-console` while `CDE_H2_CONSOLE_ENABLED` is true.

## Configuration

| Property | Default |
|---|---|
| `documentation.home` (`CDE_DOCUMENTATION_HOME`) | `~/Documents/iniciativaIADocumentación` |
| `documentation.templates.path` | `${documentation.home}/templates` |
| `documentation.prompts.path` | `${documentation.home}/prompts` |
| `documentation.skills.path` (`CDE_DOCUMENTATION_SKILLS_PATH`) | `${documentation.home}/skills` |
| `documentation.agents.<agent>.model` (`CDE_AGENT_<AGENT>_MODEL`) | One model per agent, including `editor` |
| `documentation.output-root` | `${documentation.home}/documentación` |
| `spring.datasource.url` (`CDE_DB_URL`) | H2 file in `${documentation.home}/db` |
| `app.temporal.server-target` | `127.0.0.1:7233` |

A Temporal server is required; for local development run `temporal server start-dev`.
