# AI Token Tracker

A multi-tenant proxy that sits in front of an LLM provider (Groq), authenticates callers by API key, enforces monthly spend budgets, and logs every call for cost/usage tracking.

## Tech stack

- Java 21, Spring Boot 4
- PostgreSQL (Docker)
- Spring Data JPA / Hibernate
- Bean Validation (`spring-boot-starter-validation`)
- Groq API (OpenAI-compatible LLM provider)
- Plain HTML/JS dashboard (no frontend framework)

## Features

- Multi-tenant architecture (companies, teams, API keys)
- Real-time cost/token/latency tracking per LLM call
- Hashed API keys (SHA-256) — raw keys never stored, only shown once at creation
- Budget enforcement — blocks calls before they exceed a set monthly limit
- Optimistic locking (`@Version`) with a retry loop on budget updates, to prevent two concurrent calls from corrupting the same budget's spend total
- Admin endpoints (companies, teams, budgets, dashboard) protected by a separate shared secret (`X-Admin-Key`), kept apart from the per-company `X-Api-Key` used on the proxy endpoint
- Request validation on all "create" endpoints (`@NotBlank`, `@DecimalMin`, etc.) with clean field-level error responses
- Graceful handling of upstream provider failures (rate limits, downtime)
- Dashboard with usage totals and cost breakdown by feature

---

## How to Run

### Prerequisites
- Java 21 (JDK)
- Docker Desktop (running)
- IntelliJ IDEA (or any IDE that supports Maven/Spring Boot)
- A free Groq API key — sign up at [console.groq.com](https://console.groq.com) → API Keys → Create API Key

### 1. Clone the repo
```bash
git clone https://github.com/YOUR_USERNAME/ai-token-tracker.git
cd ai-token-tracker
```

### 2. Start PostgreSQL via Docker
```bash
docker-compose up -d
```
This starts a Postgres container named `tokentrack-db` on port 5432, with a database called `tokentrack`.

Verify it's running:
```bash
docker ps
```

### 3. Set environment variables

This project reads secrets from environment variables instead of hardcoding them:

| Variable | Purpose |
|---|---|
| `DB_PASSWORD` | Your Postgres password (matches `docker-compose.yml`) |
| `API_KEY` | Your Groq API key |
| `ADMIN_API_KEY` | Shared secret required in the `X-Admin-Key` header for all `/v1/companies/**` admin endpoints |

**In IntelliJ:**
1. Click the run configuration dropdown (top right) → **Edit Configurations**
2. Select `AiTokenTrackerApplication`
3. Find **Environment variables** → click the folder icon
4. Add: `DB_PASSWORD=****`, `API_KEY=gsk_your_actual_groq_key_here`, `ADMIN_API_KEY=any-long-random-secret-you-choose`
5. Apply → OK

**If running from the command line instead**, set them in your terminal session first:
```bash
export DB_PASSWORD=****
export API_KEY=gsk_your_actual_groq_key_here
export ADMIN_API_KEY=any-long-random-secret-you-choose
```
(On Windows PowerShell: `$env:DB_PASSWORD="****"`, `$env:API_KEY="gsk_..."`, `$env:ADMIN_API_KEY="..."`)

### 4. Run the application

**In IntelliJ:** open the project, let Maven finish importing dependencies, then click the green ▶ button on `AiTokenTrackerApplication`.

**Or via terminal:**
```bash
./mvnw spring-boot:run
```

The app starts on `http://localhost:8080`. On first run, Hibernate automatically creates all required tables in Postgres.

### 5. Open the dashboard

### 6. Seed test data (via Postman or curl)

All `/v1/companies/**` routes below now require an `X-Admin-Key` header matching your `ADMIN_API_KEY`.

Create a company:
```bash
curl -X POST http://localhost:8080/v1/companies \
  -H "Content-Type: application/json" \
  -H "X-Admin-Key: YOUR_ADMIN_API_KEY" \
  -d "{\"name\": \"Acme Corp\"}"
```

Create an API key for that company (replace `{companyId}` with the id from the response above):
```bash
curl -X POST http://localhost:8080/v1/companies/{companyId}/api-keys \
  -H "Content-Type: application/json" \
  -H "X-Admin-Key: YOUR_ADMIN_API_KEY" \
  -d "{\"name\": \"test-key\"}"
```

Copy the `apiKey` value from the response — this is shown only once.

### 7. Make a test proxy call

This endpoint is public-facing and uses the per-company `X-Api-Key` instead — no `X-Admin-Key` needed here.

```bash
curl -X POST http://localhost:8080/v1/proxy/chat \
  -H "X-Api-Key: YOUR_KEY_HERE" \
  -H "X-Feature: test" \
  -H "Content-Type: application/json" \
  -d "{\"provider\": \"groq\", \"model\": \"llama-3.1-8b-instant\", \"messages\": [{\"role\": \"user\", \"content\": \"hello\"}]}"
```

You should get back the LLM's response along with token counts, cost, and latency. Refresh the dashboard with the company ID to see it reflected.

---

## API Overview

| Endpoint | Purpose | Auth |
|---|---|---|
| `POST /v1/companies` | Create a company | `X-Admin-Key` |
| `GET /v1/companies` | List all companies | `X-Admin-Key` |
| `POST /v1/companies/{id}/api-keys` | Generate an API key for a company | `X-Admin-Key` |
| `POST /v1/companies/{id}/teams` | Create a team under a company | `X-Admin-Key` |
| `GET /v1/companies/{id}/teams` | List a company's teams | `X-Admin-Key` |
| `POST /v1/companies/{id}/budgets` | Set (or update) a monthly spend limit — upsert, safe to call repeatedly | `X-Admin-Key` |
| `GET /v1/companies/{id}/budgets` | List a company's budgets | `X-Admin-Key` |
| `GET /v1/companies/{id}/dashboard/summary` | Aggregated usage/cost stats | `X-Admin-Key` |
| `POST /v1/proxy/chat` | Proxy a chat request to the LLM, with auth + budget check + logging | `X-Api-Key` (per company) |

## Validation

All "create" DTOs (`CreateCompanyRequest`, `CreateTeamRequest`, `CreateApiKeyRequest`, `CreateBudgetRequest`, `ChatRequest`) are validated with Bean Validation annotations (`@NotBlank`, `@NotEmpty`, `@NotNull`, `@DecimalMin`), activated via `@Valid` on the controller method parameters. Failures return a `400` with a field → message map, via a dedicated `MethodArgumentNotValidException` handler in `GlobalExceptionHandler`.

## Admin authentication

`/v1/companies/**` is guarded by `AdminAuthFilter`, a plain `OncePerRequestFilter` registered only for that URL pattern (see `FilterConfig`) — it checks the `X-Admin-Key` header against `admin.api.key` and short-circuits with a `401` if it's missing or wrong, before the request reaches any controller. It's intentionally not `@Component`-annotated, since that would apply it to every URL including the public `/v1/proxy/chat` endpoint. No full Spring Security dependency was introduced for this, since a single shared-secret check didn't warrant the extra footprint.

## Known limitations / what I'd do next

- Add a scheduled job to reset `currentSpendUsd` back to zero at the start of each month (`@EnableScheduling` is already on, but nothing uses it yet)
- Replace `ddl-auto=update` with Flyway/Liquibase for versioned schema migrations
- The budget check-then-act still has a narrow race window: optimistic locking + retry guarantees the running total itself is never corrupted, but two requests that both pass `checkBudget()` in the same instant could both proceed before either's spend is recorded, allowing a small overage in rare high-concurrency bursts
- Move usage ingestion to Kafka for durability and to decouple logging from the request path
- Add retry/circuit-breaker (Resilience4j) around the LLM provider call
- Add a unique DB constraint on `(company_id, team_id)` for budgets to prevent duplicates at the data layer, not just in application logic
- `ConcurrentBudgetUpdateException` is defined but unused — `BudgetService` still re-throws the raw `OptimisticLockingFailureException` after exhausting retries, which isn't caught by `GlobalExceptionHandler` and would surface as a generic 500
- No admin-side authentication for *who* the admin is — `X-Admin-Key` is a single shared secret, not per-admin credentials; fine for a small internal team, wouldn't scale to multiple admins needing individual accountability/revocation
- Expand test coverage to `BudgetService.recordSpend()` specifically, simulating concurrent updates to verify the retry loop behaves as intended — currently the most business-critical logic in the app with no dedicated test

## Notable bugs fixed during development

- A Windows/JDK timezone name mismatch (`Asia/Calcutta` vs `Asia/Kolkata`) caused Hibernate to fail connecting to Postgres — fixed by forcing the JVM default timezone to UTC at startup
- After adding API key hashing, authentication silently broke because the lookup compared a raw key against a stored hash — the write path was updated but the read path wasn't, a classic partial-migration bug
- Duplicate company-wide budgets could be created, causing the budget check to arbitrarily pick an old/exhausted one — fixed by making budget creation idempotent (update in place instead of inserting duplicates)
- `spring-boot-starter-validation` was on the classpath but never wired up — no DTO had validation annotations and no controller used `@Valid`, so empty/invalid input (blank names, zero or negative budgets) was silently accepted. Added `@NotBlank`/`@NotEmpty`/`@DecimalMin` to all "create" DTOs, `@Valid` on the matching controller methods, and a `MethodArgumentNotValidException` handler
- All `/v1/companies/**` endpoints (create company, mint API keys, view any company's dashboard, etc.) had no authentication at all — any caller who knew or guessed a `companyId` could act on it. Added `AdminAuthFilter`, scoped only to that URL pattern, requiring a shared `X-Admin-Key` secret

## Testing

- Unit tests: `PricingServiceTest` (cost calculation, including fallback rate and zero-token edge case)
- `AiTokenTrackerApplicationTests` — Spring context smoke test