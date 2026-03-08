# Claude Code — PR Code Review Agent

## Project Identity

| Attribute | Value |
|-----------|-------|
| **Purpose** | AI-powered GitHub PR review agent |
| **Stack** | Java 25 / Spring Boot 4.0.3 · Embabel Agent 0.3.4 · Spring AI · WebClient |
| **Architecture** | Hexagonal (domain / infrastructure / agent layers) |
| **Base package** | `com.stablebridge.prreview` |
| **Build** | Gradle 9.x (Kotlin DSL) |
| **Linear project** | PR Code Review Agent |

## Coding Preferences (Non-Negotiable)

| Preference | Rule |
|-----------|------|
| **Java version** | **Java 25** + Gradle 9.x |
| **Gradle DSL** | **Kotlin DSL** (`build.gradle.kts` / `settings.gradle.kts`) |
| **Imports** | **No wildcard imports** — every import fully qualified and explicit |
| **Static imports** | **Use static imports** wherever they improve readability |
| **Build cache** | Always include `buildCache { local { isEnabled = true } }` in `settings.gradle` |
| **Test assertions** | **Single-assert pattern MANDATORY.** Build expected object → single `assertThat(actual).usingRecursiveComparison().ignoringFields(...).isEqualTo(expected)`. |
| **No generic matchers** | **NEVER use `any()`, `anyString()`, `eq()`**. Always pass actual values. Only allowed: `eqIgnoringTimestamps(expected)` and `eqIgnoring(expected, "field1")` from `TestUtils`. |
| **Test naming** | `should*` camelCase with Given/When/Then structure |
| **Test fixtures** | All factory methods in `src/testFixtures/java/.../fixtures/*Fixtures.java` |
| **BDD Mockito** | `given().willReturn()`, `then().should()` |
| **Hexagonal purity** | Domain layer has ZERO deps on infrastructure or agent packages (enforced by ArchUnit) |
| **`@Builder(toBuilder=true)`** | On all domain records |
| **`@RequiredArgsConstructor`** | On all injectable classes |
| **`@ConfigurationProperties` records** | With compact constructor defaults |
| **Infrastructure adapters** | `@Component` (not `@Service`), package-private class |

## Architecture

```
agent/                    # Embabel @Agent — orchestrates GOAP flow
├── PrReviewAgent.java    #   4 @Action methods, @AchievesGoal
└── ReviewPersonas.java   #   LLM persona definitions

domain/                   # Pure domain — ZERO infra deps
├── model/                #   Records, enums
├── port/                 #   PullRequestProvider interface
└── service/              #   ReviewFormatter

infrastructure/           # Adapters implementing domain ports
└── github/               #   GitHubPullRequestAdapter, config, properties
```

## Session Start (Mandatory)

1. Read `IMPLEMENTATION_STATE.md` and `IMPLEMENTATION_STATE.json`.
2. Read `tasks/lessons.md` if it exists.
3. Resume from current state.

## Execution Rules

- Create/update `tasks/todo.md` checklist before any non-trivial work.
- Keep exactly **one** task `in_progress` at a time.
- Prefer root-cause fixes. Keep diffs minimal and localized.
- After any correction: document in `tasks/lessons.md`.

## Completion Rules (NON-NEGOTIABLE)

1. Never mark done without evidence (tests pass, output verified).
2. **BEFORE stopping**, update ALL of:
   - `IMPLEMENTATION_STATE.md`
   - `IMPLEMENTATION_STATE.json`
   - `tasks/todo.md`
3. Keep the resume snapshot current.

## Workflow — Linear + GitHub

| Tool | Role |
|------|------|
| **Linear** | Track issues (team: StableCoinPayments, project: PR Code Review Agent) |
| **GitHub** | Code, CI, PRs |

### Branch naming
```
feature/STA-<id>-<short-description>
```

### PR body
Always include `Closes STA-<id>` to auto-close the Linear issue on merge.

## Conflict Resolution Order

1. Direct user instruction → 2. This file → 3. Generic best practices
