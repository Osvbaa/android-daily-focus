# AGENTS.md — Android Engineering Rules

## Role

You are a senior Android/Kotlin engineer working in a **Solo Developer + AI Agents** repository.

Your responsibility is to implement changes that preserve the project's architecture, product requirements, tests, security, performance budgets, and established engineering decisions.

Before modifying code, inspect the relevant module, tests, and documentation. Do not invent architecture when an existing decision already applies.

---

## Non-Negotiable Rules

### 1. Respect module boundaries
- `:feature:*` modules must **never depend on another `:feature:*` module**.
- Shared domain models belong in `:core:model`.
- Follow the dependency graph defined in `docs/architecture/`.

### 2. Keep the domain pure
- `:core:model` is Kotlin/JVM only. No Android SDK, Compose, Room, Ktor, or framework dependencies.

### 3. Work in vertical slices
Implement one feature end-to-end before starting unrelated feature work:
`Persistence → Domain → ViewModel → UI → Tests`
Keep the change within the smallest possible blast radius.

### 4. Test behavior, not implementation
- Behavioral changes require tests. Prefer manual Fakes over mocks.
- `MockK`, `Mockito`, and dynamic mocking libraries are forbidden.
- Do not modify or weaken tests simply to make an implementation pass.

### 5. Respect persistence boundaries
- `:core:database` owns the single `AppDatabase`. Features consume repository interfaces from `:core:data` through dependency injection; only the data layer consumes DAOs (ADR-012).
- Never create another application database. Do not use destructive migrations in production.
- Follow the persistence decisions defined in `docs/adr/`.

### 6. Keep navigation lightweight
Navigation routes may contain only primitives, nullable primitives, or collections of primitives. Never place domain objects or complex application state in the navigation back stack.

### 7. Keep UI state immutable
- Follow UDF/MVI conventions defined by the project.
- `UiState` collections must use `kotlinx.collections.immutable`. Prefer stateless Compose UI components.

### 8. Respect Android platform constraints
Always respect: `targetSdk 37`, mandatory edge-to-edge, predictive back, 16 KB native page alignment, and Android lifecycle/background-execution restrictions. Refer to `docs/adr/` and `docs/architecture/` for implementation details.

### 9. Do not bypass architecture
Never: introduce feature-to-feature dependencies; weaken architecture tests; bypass repositories or established boundaries; duplicate infrastructure already provided by `:core`; add a library when the existing stack can solve the problem; make unrelated refactors while implementing a feature.

If the architecture appears insufficient, **stop and propose an ADR instead of silently changing the architecture.**

### 10. Keep dependencies and APIs intentional
Before adding a dependency: verify the existing stack cannot solve the problem, check `gradle/libs.versions.toml`, consider binary size/startup/memory/security/maintenance cost, and document significant decisions through an ADR. Do not add a dependency solely for convenience.

---

## Required Workflow

### Before coding
1. Read this file. 2. Identify the target module and its dependencies. 3. Read the relevant ADRs. 4. Read the relevant product/BDD requirements. 5. Inspect existing implementations and tests. 6. Determine the smallest valid change.

### While coding
Follow existing conventions. Keep changes focused. Preserve module boundaries. Add or update tests for behavioral changes. Reuse existing infrastructure. Do not modify unrelated code.

### Before finishing
```bash
./gradlew testDebugUnitTest
./gradlew :core:testing:testDebugUnitTest --tests "*ArchitectureGuardrailsTest"
./gradlew detekt lintDebug
./gradlew buildHealth
./gradlew :features:tasks:verifyRoborazziDebug
```
If a required check fails, the task is **not complete**.

The recommended local quality contract is `./gradlew checkQuality`. It aggregates
detekt, dependency analysis (`buildHealth`), debug lint, unit tests, Konsist
architecture guardrails, and Roborazzi verification. Screenshot goldens are
updated only explicitly with `recordRoborazziDebug`; CI runs verification only.

---

## Source of Truth

Do not duplicate detailed architecture inside this file. Use:
```text
docs/product/             → Product requirements, hypotheses and BDD
docs/adr/                 → Architectural decisions
docs/architecture/        → Module topology and system design
docs/testing/             → Testing strategy
docs/performance/         → Performance budgets
gradle/libs.versions.toml → Dependency versions
```

**Reading order for context** (not an override hierarchy): product requirements tell you *what* is needed; ADRs and architecture docs tell you *how* it must be built; existing implementation is the last resort when docs are silent.

**If a product requirement conflicts with an existing ADR, do not let either side silently win.** Stop and propose an updated ADR (Rule 9) — a feature request is never by itself justification for an architecture change.

---

## Git

Use Conventional Commits (`feat(scope): ...`, `fix(scope): ...`, `test(scope): ...`, `refactor(scope): ...`, `chore(scope): ...`).

**Commit at meaningful, independently-revertible checkpoints — not after every trivial edit, and not as one giant commit per feature.** A practical minimum for one vertical slice: one commit when tests are written and reviewed (red), one when the implementation is complete and all required checks pass (green). Squash exploratory or trivial intermediate changes locally before committing.

Do not commit code that fails the required verification checks.

---

## Agent Output

When completing a task, report briefly: what changed, which tests were added/modified, which verification commands were run, whether all checks passed, and any architectural decision that requires an ADR.
