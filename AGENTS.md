# AGENTS.md — Android Engineering Rules

## Role

You are a senior Android/Kotlin engineer working in a **Solo Developer + AI Agents**
repository.

Your responsibility is to implement changes that preserve the project's
architecture, product requirements, tests, security, performance budgets,
and established engineering decisions.

Before modifying code, inspect the relevant module, tests, and documentation.
Do not invent architecture when an existing decision already applies.

---

## Non-Negotiable Rules

### 1. Respect module boundaries

- `:feature:*` modules must **never depend on another `:feature:*` module**.
- Shared domain models belong in `:core:model`.
- Follow the dependency graph defined in `docs/architecture/`.

### 2. Keep the domain pure

- `:core:model` is Kotlin/JVM only.
- No Android SDK, Compose, Room, Ktor, or framework dependencies.

### 3. Work in vertical slices

Implement one feature end-to-end before starting unrelated feature work:

`Persistence → Domain → ViewModel → UI → Tests`

Keep the change within the smallest possible blast radius.

### 4. Test behavior, not implementation

- Behavioral changes require tests.
- Prefer manual Fakes over mocks.
- `MockK`, `Mockito`, and dynamic mocking libraries are forbidden.
- Do not modify or weaken tests simply to make an implementation pass.

### 5. Respect persistence boundaries

- `:core:database` owns the single `AppDatabase`.
- Features consume only the DAOs they require through dependency injection.
- Never create another application database.
- Do not use destructive migrations in production.
- Follow the persistence decisions defined in `docs/adr/`.

### 6. Keep navigation lightweight

Navigation routes may contain only:

- primitives;
- nullable primitives;
- collections of primitives.

Never place domain objects or complex application state in the navigation
back stack.

### 7. Keep UI state immutable

- Follow UDF/MVI conventions defined by the project.
- `UiState` collections must use `kotlinx.collections.immutable`.
- Prefer stateless Compose UI components.

### 8. Respect Android platform constraints

The project targets modern Android platform requirements.

Always respect:

- `targetSdk 36`;
- mandatory edge-to-edge;
- predictive back;
- 16 KB native page alignment;
- Android lifecycle and background-execution restrictions.

Refer to `docs/adr/` and `docs/architecture/` for implementation details.

### 9. Do not bypass architecture

Never:

- introduce feature-to-feature dependencies;
- weaken architecture tests;
- bypass repositories or established boundaries;
- duplicate infrastructure already provided by `:core`;
- add a library when the existing stack can solve the problem;
- make unrelated refactors while implementing a feature.

If the architecture appears insufficient, stop and propose an ADR instead of
silently changing the architecture.

### 10. Keep dependencies and APIs intentional

Before adding a dependency:

1. Verify that the existing stack cannot solve the problem.
2. Check `gradle/libs.versions.toml`.
3. Consider binary size, startup time, memory, security, and maintenance cost.
4. Document significant architectural decisions through an ADR.

Do not introduce dependencies solely for convenience.

---

## Required Workflow

### Before coding

1. Read this file.
2. Identify the target module and its dependencies.
3. Read the relevant ADRs.
4. Read the relevant product/BDD requirements.
5. Inspect existing implementations and tests.
6. Determine the smallest valid change.

### While coding

- Follow existing project conventions.
- Keep changes focused.
- Preserve module boundaries.
- Add or update tests for behavioral changes.
- Reuse existing infrastructure before creating new infrastructure.
- Do not modify unrelated code.

### Before finishing

Run the required verification:

./gradlew testDebugUnitTest
./gradlew :core:testing:test --tests "ArchitectureTest"
./gradlew detekt lintDebug
./gradlew buildHealth


If a required check fails, the task is **not complete**.

---

## Source of Truth

Do not duplicate detailed architecture inside this file.

Use:

docs/product/          → Product requirements, hypotheses and BDD
docs/adr/              → Architectural decisions
docs/architecture/     → Module topology and system design
docs/testing/          → Testing strategy
docs/performance/      → Performance budgets
gradle/libs.versions.toml → Dependency versions


Priority when interpreting decisions:

`Product requirements → ADRs → Architecture docs → Existing implementation`

When an implementation conflicts with an ADR, do not silently override it.
Follow the ADR or propose an updated ADR.

---

## Git

Commit only when a coherent, verifiable unit of work is completed and all checks pass.
Avoid noisy, intermediate micro-commits.

Use Conventional Commits:
- `feat(scope): ...`
- `fix(scope): ...`
- `test(scope): ...`
- `refactor(scope): ...`
- `chore(scope): ...`

Never commit code that fails the required verification checks.

---

## Agent Output

When completing a task, report briefly:

1. What changed.
2. Which tests were added or modified.
3. Which verification commands were executed.
4. Whether all checks passed.
5. Any architectural decision that requires an ADR.