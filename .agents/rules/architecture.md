# Architecture & Code Quality Rules — agenteIAPublico

These rules apply to every code change, code generation, or suggestion made in
this project. They are mandatory and non-negotiable. Violations must be
corrected before delivering any response.

---

## 1. Clean Architecture — Layer boundaries

The project follows **Clean Architecture**. Every class must live in the correct
layer and must never violate the dependency rule (outer layers depend on inner
layers, never the reverse).

| Layer | Package | Allowed dependencies |
|---|---|---|
| **Entrypoint (HTTP)** | `controller/` | `service/` interfaces, `domain/`, `exception/` |
| **Orchestration (Temporal)** | `workflow/`, `activity/` | `service/`, `copilot/` interfaces, `domain/` |
| **Domain interfaces** | `service/` | `domain/` only |
| **Domain implementations** | `service/impl/`, `service/strategy/` | `service/` interfaces, `domain/`, `helper/`, `repository/`, `exception/` |
| **Documentation pipeline** | `copilot/`, `documentation/`, `knowledge/` | `domain/`, `repository/`, `util/`, their own sub-packages |
| **Infrastructure helpers** | `helper/` | `domain/`, `config/`, `repository/`, `exception/` |
| **Persistence** | `repository/` (Spring Data), `domain/entity/` (JPA), `service/mapper/` | `domain/` |
| **Domain models** | `domain/`, `domain/enums/` | No dependencies on other layers |
| **Config beans** | `config/`, `*/config/` | Spring only |
| **Exceptions** | `exception/`, `*/exception/` | No dependencies on other layers |

**Rules:**
- `domain/` must never import from `copilot/` or any other outer package.
- `controller/` classes must NEVER instantiate service implementations directly.
  Always inject the interface.
- Workflow code (`workflow/impl/`) must stay deterministic: no Spring beans, I/O,
  clocks or random values outside the Temporal `Workflow` API.
- JPA entities never leave the persistence boundary: map them to domain records
  in `service/mapper/` before returning them from a service.

## 2. Single Responsibility Principle (SRP)

Each class must have **one and only one reason to change**.

- A `Controller` only handles HTTP concerns: request mapping, request/response
  transformation, and delegation to the orchestrator. No business logic.
- A `Service` interface defines the contract. No implementation detail leaks
  into the interface.
- A `ServiceImpl` contains business orchestration only. It must not perform
  I/O (file system, network, process execution) directly — delegate to `helper/`
  or `copilot/` classes.
- A `Helper` performs one specific infrastructure task (e.g.,
  `GitRepositoryCloner` clones repos, `DefaultGitCommandExecutor` executes shell
  commands).
- A `Strategy` encapsulates one variant of a behaviour (e.g., one
  `DocumentLifecycleStrategy` per lifecycle). Never use `if/else if` chains to
  select strategies — use the strategy pattern already established in
  `copilot/strategy/` and `service/strategy/`.

---

## 3. Method length — hard limit

**No method may exceed 20 lines of code.** This is a hard rule from AGENTS.md.

- If a method grows beyond 20 lines, extract private methods with descriptive
  names that explain the intent (not the implementation).
- Extraction candidates always follow the naming pattern:
  - `validate*` — guard/precondition checks
  - `build*` — object construction
  - `collect*` — aggregation loops
  - `resolve*` — lookup / fallback logic
  - `detect*` / `extract*` — parsing / pattern matching

---

## 4. Open/Closed Principle (OCP)

Always extend via new strategy classes, never by adding `if/else` dispatch chains.

- **Scan scope**: add a `ScanScopeStrategy` in `service/strategy/` (ordered with
  `@Order`; `FullScanScopeStrategy` is the fallback).
- **Document lifecycle**: add a `DocumentLifecycleStrategy` and an
  `EvidenceScopeStrategy` in `copilot/strategy/` for each `DocumentLifecycleEnum`.
- **Documentation agents**: add a `SpecialistDocumentationAgent` extending
  `AbstractSpecialistAgent` with its external prompt.
- **Code signal rules**: add a `SignalDetector` in `service/detector/` (extend
  `LinePatternSignalDetector` for line rules, ordered with `@Order`); bump
  `ANALYZER_VERSION` in `RepositoryCodeScanService` whenever a rule changes so
  stored evidence is rebuilt with a full scan.
- **Writing style**: add a skill folder (`<name>/SKILL.md`) under
  `documentation.skills.path` and list it in the template `skills`; never hard
  code writing rules in Java or in the render prompt.
- **Template keys**: a new template key must be read by a parser in
  `documentation/template/service/` and added to `TemplateKeyInspector`;
  unread keys are reported as warnings, never kept as silent configuration.

## 5. Dependency Inversion Principle (DIP)

- Always inject **interfaces**, never concrete classes, in constructor
  parameters.
- Strategies are selected by the value they declare (`lifecycle()`,
  `supports(...)`), never by runtime `if` branching on configuration values.
- New injectable components must define an interface in the parent package and
  the implementation in `impl/`.

---

## 6. DRY — Don't Repeat Yourself

- **Rule detection logic** lives exclusively in the `SignalDetector`
  implementations; evidence construction lives in `EvidenceFactory`.
- **Prompt text** lives exclusively in the external prompt and skill files
  (`documentation.prompts.path`, `documentation.skills.path`); Java code only
  renders placeholders.
- **Copilot CLI flags** live exclusively in `CopilotCliPromptExecutor`; every
  tool stays denied so the model works only with the evidence in the prompt.
- **Document paths and keys** live in `DocumentPathResolver`; versions and the
  changelog live in `ChangelogService`.
- **Git command execution** lives in `DefaultGitCommandExecutor`. No other
  class may invoke `ProcessBuilder` for git operations.
- **Hashing** goes through `util/HashUtil`.
- **Property access** must always go through `@ConfigurationProperties` beans.
  Never read `System.getenv()` or `System.getProperty()` outside a config bean.

## 7. Naming conventions (mandatory)

Follow the class naming convention from AGENTS.md exactly:

| Responsibility | Suffix | Example |
|---|---|---|
| REST controller | `Controller` | `RepositoryAgentController` |
| Service interface | `Service` | `CodeScanService` |
| Service implementation | `ServiceImpl` | `FooServiceImpl` |
| Repository | `Repository` | `ScanResultRepository` |
| Domain model | *(no suffix)* | `RepositoryCloneRequest` |
| Helper (non-static) | descriptive noun | `GitRepositoryCloner` |
| Utility (static only) | `Util` | `PathUtil` |
| Exception | `Exception` | `RepositoryCloneException` |
| Configuration bean | `Properties` or `Config` | `GitCloneProperties` |
| Mapper | `Mapper` | `CloneResponseMapper` |
| Enum | `Enum` | `PromptTypeEnum` |
| Strategy | `*Strategy` or `*TypeStrategy` | `ErrorManualDocumentTypeStrategy` |
| Workflow (Temporal) | `Workflow` | `RepositoryProcessingWorkflow` |
| Activity (Temporal) | `Activity` | `CloneRepositoryActivity` |

- Methods: `lowerCamelCase`, descriptive verb-noun pairs.
- Constants: `UPPER_SNAKE_CASE`.
- Variables: `lowerCamelCase`. No single-letter variables except loop indices.

---

## 8. Lombok usage

Always use Lombok for boilerplate. Never write manual getters, setters,
constructors, or builders for domain or DTO classes.

- `@Getter` + `@Setter` for mutable beans.
- `@Builder(toBuilder = true)` for domain objects that need copy semantics
  (e.g., `RepositoryCloneResponse.withDocuments(...)` pattern).
- Use `record` for immutable value objects (already used in `domain/`). Do not
  add Lombok annotations to records.

---

## 9. Exception handling

- Never swallow exceptions silently (`catch (Exception e) {}`).
- Infrastructure exceptions must be caught at the boundary and wrapped in a
  domain-specific exception from `exception/`
  (e.g., `RepositoryCloneException`).
- HTTP error mapping lives exclusively in `RestExceptionHandler`. No controller
  method may return error responses inline.
- Use guard clauses and early returns. Never nest more than 2 levels of `if`
  blocks inside a method.

---

## 10. Test coverage requirements

Every new public method must be accompanied by a unit test.

- Tests live under `src/test/java` mirroring the main source tree.
- Use JUnit 5 (`@Test`) + Mockito for unit tests. Do not load the Spring
  context unless integration testing is explicitly required.
- Test method names follow: `<methodName>Should<expectedBehavior>` or
  `<methodName>When<condition>`.
- Scanner tests use real temp directories (`Files.createTempDirectory`) —
  follow this pattern for any new scanner logic.
- After any change, run `./gradlew -q test` and confirm all tests pass before
  delivering the response.

---

## 11. Orchestration is always Temporal

- Every documentation execution runs as the `RepositoryProcessingWorkflow`.
  There is no direct (non-Temporal) processing mode.
- The workflow id is per repository, so a second request for the same
  repository while one is running is rejected with HTTP 409.
- Long or non-deterministic work (git, file system, database, Copilot CLI) must
  run inside activities, never inside workflow code.

## 12. Configuration & environment variables

- All configuration must be declared in `application.yml` with environment
  variable placeholders (`${ENV_VAR:default}`).
- New properties must be added to the appropriate `@ConfigurationProperties`
  class, never accessed via `@Value` in service classes.
- Sensitive values (PAT, passwords) must never be logged. Ensure
  `GitCloneProperties.repositoryUrl()` output is never passed to any logger.

---

## 13. Line and formatting rules

- Maximum **100 columns** per line.
- Indentation: **2 spaces**. Wrapped lines use **4 spaces**.
- Remove all unused imports when modifying a file.
- No comments inside method bodies. Only service interface methods may have
  Javadoc.
- All source code and comments must be written in **English**.
