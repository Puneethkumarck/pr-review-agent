# PR Code Review Agent

AI-powered GitHub pull request review agent built with [Embabel Agent Framework](https://github.com/embabel/embabel-agent) and Spring AI. Automatically fetches PR diffs, analyzes code using LLM, and posts review comments — all orchestrated via Goal-Oriented Action Planning (GOAP).

## How It Works

```
UserInput ──▶ parsePrInput (LLM) ──▶ fetchPrDiff ──▶ analyzeCode (LLM) ──▶ postReview
                                         │                                      │
                                    GitHub API                             GitHub API
                                   (fetch diff)                          (post review)
```

The agent uses GOAP to automatically plan and execute the review pipeline:

1. **Parse PR Input** — LLM extracts `owner/repo#number` from natural language or URL
2. **Fetch PR Diff** — Calls GitHub API v3 to get PR metadata, files, and patches
3. **Analyze Code** — LLM reviews the diff with a Senior Code Reviewer persona, identifying bugs, security issues, performance problems, and style suggestions
4. **Post Review** — Formats the analysis as markdown and posts it as a PR review comment

## Tech Stack

| Component | Technology |
|-----------|-----------|
| Runtime | Java 25 |
| Framework | Spring Boot 4.0.3 |
| AI Agent | Embabel Agent Framework 0.3.4 |
| LLM | Spring AI (Anthropic / OpenAI) |
| HTTP Client | Spring WebFlux (WebClient) |
| Shell | Spring Shell 3.4 |
| Build | Gradle 9.x (Kotlin DSL) |
| Architecture | Hexagonal (ports & adapters) |
| Testing | JUnit 5, WireMock, ArchUnit, BDD Mockito |

## Quick Start

### Prerequisites

- Java 25+
- A GitHub personal access token
- An LLM API key (Anthropic or OpenAI)

### Run

```bash
export GITHUB_TOKEN=ghp_your_token
export ANTHROPIC_API_KEY=sk-ant-your-key   # or OPENAI_API_KEY

./scripts/shell.sh
```

This starts an interactive shell where you can:

```
embabel> fetch-pr --owner acme --repo webapp --pr 42

--- PR #42: Fix null pointer in payment processing ---
Description: Handles the case where merchant config is absent
Branch: fix/npe-merchant-config → main
Files changed: 2

  src/main/java/com/example/Service.java             [modified] +10/-3
  src/test/java/com/example/ServiceTest.java          [added   ] +45/-0
```

### REST API

The agent also exposes REST endpoints:

```bash
# Review a PR
curl -X POST http://localhost:8080/api/v1/reviews \
  -H 'Content-Type: application/json' \
  -d '{"owner": "acme", "repo": "webapp", "prNumber": 42}'
```

**Response:**
```json
{
  "owner": "acme",
  "repo": "webapp",
  "prNumber": 42,
  "title": "Fix null pointer in payment processing",
  "filesChanged": 2,
  "reviewBody": "## PR Summary: Fix null pointer...",
  "posted": true
}
```

### GitHub Webhook

Configure a webhook in your GitHub repository to auto-review PRs on open:

1. **Payload URL:** `https://your-server/api/v1/webhook`
2. **Content type:** `application/json`
3. **Secret:** Set `GITHUB_WEBHOOK_SECRET` env var (optional)
4. **Events:** Select "Pull requests"

The agent processes `opened` and `synchronize` events, ignores everything else. Webhook signatures are verified using HMAC-SHA256 with constant-time comparison.

## Architecture

```
com.stablebridge.prreview
├── agent/                          # Embabel GOAP agent
│   ├── PrReviewAgent.java          #   4 @Action methods, @AchievesGoal
│   └── ReviewPersonas.java         #   CODE_REVIEWER persona
│
├── application/controller/         # REST & webhook endpoints
│   ├── ReviewController.java       #   POST /api/v1/reviews
│   ├── WebhookController.java      #   POST /api/v1/webhook
│   ├── ReviewRequest.java          #   Request DTO
│   └── ReviewResponse.java         #   Response DTO
│
├── domain/                         # Pure domain — zero infra deps
│   ├── model/                      #   7 records + 2 enums
│   │   ├── PullRequestInput        #     owner, repo, prNumber
│   │   ├── PrDiff                  #     title, description, branches, files
│   │   ├── FileChange              #     filename, status, additions, deletions, patch
│   │   ├── CodeAnalysis            #     issues, suggestions, overallAssessment
│   │   ├── CodeIssue               #     filename, severity, category, description
│   │   ├── ReviewSuggestion        #     filename, description, suggestedCode
│   │   ├── CompletedReview         #     pullRequest, analysis, reviewBody, posted
│   │   ├── IssueSeverity           #     CRITICAL, WARNING, INFO
│   │   └── IssueCategory           #     BUG, SECURITY, PERFORMANCE, STYLE, MAINTAINABILITY
│   ├── port/
│   │   └── PullRequestProvider     #   Domain port interface
│   └── service/
│       └── ReviewFormatter         #   Formats analysis → markdown
│
├── infrastructure/github/          # GitHub API adapter
│   ├── GitHubPullRequestAdapter    #   Implements PullRequestProvider
│   ├── GitHubConfig                #   WebClient bean with Bearer auth
│   └── GitHubProperties            #   @ConfigurationProperties
│
└── shell/
    └── ReviewCommands              #   fetch-pr Spring Shell command
```

### Hexagonal Rules (ArchUnit enforced)

- Domain has **zero** dependencies on infrastructure, agent, application, or shell
- Domain does **not** use Spring Web, WebClient, or framework interfaces
- Infrastructure does **not** depend on agent layer

## Configuration

```yaml
app:
  github:
    token: ${GITHUB_TOKEN}                    # Required — GitHub API token
    api-url: https://api.github.com           # Default GitHub API URL
  webhook:
    secret: ${GITHUB_WEBHOOK_SECRET:}         # Optional — HMAC-SHA256 secret
```

| Environment Variable | Required | Description |
|---------------------|----------|-------------|
| `GITHUB_TOKEN` | Yes | GitHub personal access token for API calls |
| `ANTHROPIC_API_KEY` | One of | Anthropic API key for Claude |
| `OPENAI_API_KEY` | these | OpenAI API key for GPT |
| `GITHUB_WEBHOOK_SECRET` | No | Webhook signature verification secret |

## Testing

```bash
./gradlew check    # Runs unit + integration tests + Spotless
```

### Test Suite — 30 tests

| Test Class | Count | Type | Description |
|-----------|-------|------|-------------|
| `ArchitectureTest` | 7 | ArchUnit | Hexagonal layer isolation rules |
| `ReviewFormatterTest` | 4 | Unit | Markdown formatting (summary + full review) |
| `PrReviewAgentTest` | 3 | Unit | Agent actions (BDD Mockito) |
| `GitHubPullRequestAdapterTest` | 3 | Unit | GitHub API adapter (WireMock) |
| `ReviewControllerTest` | 2 | Unit | REST endpoint (WebTestClient) |
| `WebhookControllerTest` | 4 | Unit | Webhook event handling |
| `WebhookSignatureTest` | 4 | Unit | HMAC-SHA256 verification |
| `ReviewCommandsTest` | 1 | Unit | Shell command output |
| `PrReviewAgentIntegrationTest` | 3 | Integration | Full GOAP chain (EmbabelMockitoIntegrationTest) |

### Test Conventions

- **Single-assert pattern** — build expected object, single `assertThat(actual).usingRecursiveComparison().isEqualTo(expected)`
- **BDD Mockito** — `given().willReturn()`, `then().should()`
- **No generic matchers** — actual values in stubs and verifications
- **Test fixtures** — all factory methods in `src/testFixtures/java/.../fixtures/`

## Building

```bash
./gradlew build              # Compile + test + spotless
./gradlew test               # Unit tests only
./gradlew integrationTest    # Integration tests only
./gradlew spotlessApply      # Fix formatting
./gradlew bootRun            # Run the application
```

## License

MIT
